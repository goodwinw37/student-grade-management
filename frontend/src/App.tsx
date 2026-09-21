import {
  type ChangeEvent,
  type FormEvent,
  useCallback,
  useEffect,
  useMemo,
  useState,
} from 'react'
import './App.css'
import { addGrade, deleteGrade, getGrades, getMyGrades } from './api/grades'
import {
  createModule,
  createRegistration,
  createStudent,
  getModules,
  getRegistrations,
  getStudents,
} from './api/resources'
import { getModuleAverages, getStudentAverages } from './api/analytics'
import type {
  Grade,
  Module,
  ModuleAverage,
  Registration,
  Student,
  StudentAverage,
} from './types'
import { setBasicAuth } from './api/client'

type AsyncState = 'idle' | 'loading' | 'error'
type FormStatus = { type: 'idle' | 'success' | 'error'; message?: string }
type UserRole = 'ADMIN' | 'TEACHER' | 'STUDENT'
type DemoUser = { username: string; password: string; role: UserRole; label: string }

const semesterOptions = ['Semester 1', 'Semester 2', 'Summer'] as const
const demoAdminPassword = import.meta.env.VITE_DEMO_ADMIN_PASSWORD ?? 'dev-admin'
const demoTeacherPassword = import.meta.env.VITE_DEMO_TEACHER_PASSWORD ?? 'dev-teacher'
const demoStudentPassword = import.meta.env.VITE_DEMO_STUDENT_PASSWORD ?? 'dev-student'

const demoUsers: DemoUser[] = [
  { username: 'admin', password: demoAdminPassword, role: 'ADMIN', label: 'Admin' },
  { username: 'teacher', password: demoTeacherPassword, role: 'TEACHER', label: 'Teacher' },
  { username: 'ada', password: demoStudentPassword, role: 'STUDENT', label: 'Student (Ada)' },
  { username: 'alan', password: demoStudentPassword, role: 'STUDENT', label: 'Student (Alan)' },
  { username: 'grace', password: demoStudentPassword, role: 'STUDENT', label: 'Student (Grace)' },
]

function App() {
  const [students, setStudents] = useState<Student[]>([])
  const [modules, setModules] = useState<Module[]>([])
  const [registrations, setRegistrations] = useState<Registration[]>([])
  const [grades, setGrades] = useState<Grade[]>([])
  const [moduleAverages, setModuleAverages] = useState<ModuleAverage[]>([])
  const [studentAverages, setStudentAverages] = useState<StudentAverage[]>([])
  const [loadState, setLoadState] = useState<AsyncState>('loading')
  const [loadError, setLoadError] = useState<string | null>(null)

  const [gradeForm, setGradeForm] = useState({
    studentId: '',
    moduleCode: '',
    score: '',
    academicYear: '',
    semester: '',
  })
  const [gradeFormStatus, setGradeFormStatus] = useState<FormStatus>({ type: 'idle' })
  const [isGradeSubmitting, setIsGradeSubmitting] = useState(false)

  const [studentForm, setStudentForm] = useState({
    firstName: '',
    lastName: '',
    username: '',
    email: '',
  })
  const [studentFormStatus, setStudentFormStatus] = useState<FormStatus>({ type: 'idle' })
  const [isStudentSubmitting, setIsStudentSubmitting] = useState(false)

  const [moduleForm, setModuleForm] = useState({
    code: '',
    name: '',
    mnc: false,
  })
  const [moduleFormStatus, setModuleFormStatus] = useState<FormStatus>({ type: 'idle' })
  const [isModuleSubmitting, setIsModuleSubmitting] = useState(false)

  const [registrationForm, setRegistrationForm] = useState({
    studentId: '',
    moduleCode: '',
  })
  const [registrationFormStatus, setRegistrationFormStatus] = useState<FormStatus>({
    type: 'idle',
  })
  const [isRegistrationSubmitting, setIsRegistrationSubmitting] = useState(false)

  const [activeUser, setActiveUser] = useState<DemoUser>(demoUsers[0])
  const [authError, setAuthError] = useState<string | null>(null)
  const [gradePendingDeletion, setGradePendingDeletion] = useState<Grade | null>(null)
  const [isDeletingGrade, setIsDeletingGrade] = useState(false)
  const [deleteError, setDeleteError] = useState<string | null>(null)

  const canManage = activeUser.role === 'ADMIN'
  const canRecordGrades = activeUser.role === 'ADMIN' || activeUser.role === 'TEACHER'
  const usingStudentView = activeUser.role === 'STUDENT'

  const refreshAll = useCallback(
    async (role: UserRole = activeUser.role) => {
      setLoadState('loading')
      setLoadError(null)
      setAuthError(null)
      try {
        const gradeFetcher = role === 'STUDENT' ? getMyGrades : getGrades
        const registrationPromise =
          role === 'STUDENT' ? Promise.resolve<Registration[]>([]) : getRegistrations()
        const [
          studentData,
          moduleData,
          gradeData,
          registrationData,
          moduleAvgData,
          studentAvgData,
        ] = await Promise.all([
          getStudents(),
          getModules(),
          gradeFetcher(),
          registrationPromise,
          getModuleAverages(),
          getStudentAverages(),
        ])
        setStudents(studentData)
        setModules(moduleData)
        setRegistrations(registrationData)
        setGrades(gradeData)
        setModuleAverages(moduleAvgData)
        setStudentAverages(studentAvgData)
        setLoadState('idle')
      } catch (error) {
        setLoadError((error as Error).message)
        setAuthError((error as Error).message)
        setLoadState('error')
      }
    },
    [activeUser.role]
  )

  useEffect(() => {
    setBasicAuth(activeUser.username, activeUser.password)
    void refreshAll(activeUser.role)
  }, [activeUser, refreshAll])

  async function handleGradeSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!canRecordGrades) {
      setGradeFormStatus({
        type: 'error',
        message: 'You need teacher or admin access to record grades.',
      })
      return
    }
    if (
      !gradeForm.studentId ||
      !gradeForm.moduleCode ||
      !gradeForm.score ||
      !gradeForm.academicYear ||
      !gradeForm.semester
    ) {
      setGradeFormStatus({ type: 'error', message: 'Complete all fields first.' })
      return
    }
    setIsGradeSubmitting(true)
    setGradeFormStatus({ type: 'idle' })
    try {
      await addGrade({
        studentId: Number(gradeForm.studentId),
        moduleCode: gradeForm.moduleCode,
        score: Number(gradeForm.score),
        academicYear: gradeForm.academicYear,
        semester: gradeForm.semester,
      })
      setGradeFormStatus({ type: 'success', message: 'Grade saved successfully.' })
      setGradeForm((previous) => ({
        ...previous,
        score: '',
        academicYear: '',
        semester: '',
      }))
      const gradeFetcher = usingStudentView ? getMyGrades : getGrades
      const [updatedGrades, moduleAvgData, studentAvgData] = await Promise.all([
        gradeFetcher(),
        getModuleAverages(),
        getStudentAverages(),
      ])
      setGrades(updatedGrades)
      setModuleAverages(moduleAvgData)
      setStudentAverages(studentAvgData)
    } catch (error) {
      setGradeFormStatus({ type: 'error', message: (error as Error).message })
    } finally {
      setIsGradeSubmitting(false)
    }
  }

  function promptGradeDeletion(selectedGrade: Grade) {
    setDeleteError(null)
    setGradePendingDeletion(selectedGrade)
  }

  function dismissDeleteModal() {
    if (isDeletingGrade) {
      return
    }
    setGradePendingDeletion(null)
  }

  async function handleConfirmDelete() {
    if (!gradePendingDeletion) {
      return
    }
    setIsDeletingGrade(true)
    setDeleteError(null)
    try {
      await deleteGrade(gradePendingDeletion.id)
      const gradeFetcher = usingStudentView ? getMyGrades : getGrades
      const [updatedGrades, moduleAvgData, studentAvgData] = await Promise.all([
        gradeFetcher(),
        getModuleAverages(),
        getStudentAverages(),
      ])
      setGrades(updatedGrades)
      setModuleAverages(moduleAvgData)
      setStudentAverages(studentAvgData)
      setGradePendingDeletion(null)
    } catch (error) {
      setDeleteError((error as Error).message)
    } finally {
      setIsDeletingGrade(false)
    }
  }

  async function handleStudentSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!canManage) {
      setStudentFormStatus({
        type: 'error',
        message: 'Only admins can create or edit student records.',
      })
      return
    }
    setIsStudentSubmitting(true)
    setStudentFormStatus({ type: 'idle' })
    try {
      await createStudent(studentForm)
      setStudentFormStatus({ type: 'success', message: 'Student added successfully.' })
      setStudentForm({ firstName: '', lastName: '', username: '', email: '' })
      const updatedStudents = await getStudents()
      setStudents(updatedStudents)
    } catch (error) {
      setStudentFormStatus({ type: 'error', message: (error as Error).message })
    } finally {
      setIsStudentSubmitting(false)
    }
  }

  async function handleModuleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!canManage) {
      setModuleFormStatus({
        type: 'error',
        message: 'Only admins can create or edit modules.',
      })
      return
    }
    setIsModuleSubmitting(true)
    setModuleFormStatus({ type: 'idle' })
    try {
      await createModule({
        code: moduleForm.code,
        name: moduleForm.name,
        mnc: moduleForm.mnc,
      })
      setModuleFormStatus({ type: 'success', message: 'Module added successfully.' })
      setModuleForm({ code: '', name: '', mnc: false })
      const updatedModules = await getModules()
      setModules(updatedModules)
    } catch (error) {
      setModuleFormStatus({ type: 'error', message: (error as Error).message })
    } finally {
      setIsModuleSubmitting(false)
    }
  }

  function handleGradeInputChange(event: ChangeEvent<HTMLSelectElement | HTMLInputElement>) {
    const { name, value } = event.target
    setGradeForm((previous) => ({ ...previous, [name]: value }))
  }

  function handleStudentInputChange(event: ChangeEvent<HTMLInputElement>) {
    const { name, value } = event.target
    setStudentForm((previous) => ({ ...previous, [name]: value }))
  }

  function handleModuleInputChange(event: ChangeEvent<HTMLInputElement>) {
    const { name, value, type, checked } = event.target
    setModuleForm((previous) => ({
      ...previous,
      [name]: type === 'checkbox' ? checked : value,
    }))
  }

  function handleRegistrationInputChange(event: ChangeEvent<HTMLSelectElement>) {
    const { name, value } = event.target
    setRegistrationForm((previous) => ({ ...previous, [name]: value }))
  }

  async function handleRegistrationSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!canManage) {
      setRegistrationFormStatus({
        type: 'error',
        message: 'Only admins can register students to modules.',
      })
      return
    }
    if (!registrationForm.studentId || !registrationForm.moduleCode) {
      setRegistrationFormStatus({
        type: 'error',
        message: 'Choose a student and module first.',
      })
      return
    }
    setIsRegistrationSubmitting(true)
    setRegistrationFormStatus({ type: 'idle' })
    try {
      await createRegistration({
        studentId: Number(registrationForm.studentId),
        moduleCode: registrationForm.moduleCode,
      })
      setRegistrationFormStatus({ type: 'success', message: 'Student registered successfully.' })
      setRegistrationForm({ studentId: '', moduleCode: '' })
      const updatedRegistrations = await getRegistrations()
      setRegistrations(updatedRegistrations)
    } catch (error) {
      setRegistrationFormStatus({ type: 'error', message: (error as Error).message })
    } finally {
      setIsRegistrationSubmitting(false)
    }
  }

  function handleUserChange(event: ChangeEvent<HTMLSelectElement>) {
    const selectedUser = demoUsers.find((user) => user.username === event.target.value)
    if (selectedUser) {
      setActiveUser(selectedUser)
    }
  }

  const isLoading = loadState === 'loading'

  const gradeModuleOptions = useMemo<Module[]>(() => {
    if (!gradeForm.studentId) {
      return []
    }
    const studentId = Number(gradeForm.studentId)
    const registeredCodes = new Set(
      registrations
        .filter((registration) => registration.studentRecordId === studentId)
        .map((registration) => registration.moduleCode)
    )
    return modules.filter((module) => registeredCodes.has(module.code))
  }, [gradeForm.studentId, modules, registrations])

  const registrationModuleOptions = useMemo<Module[]>(() => {
    if (!registrationForm.studentId) {
      return []
    }
    const studentId = Number(registrationForm.studentId)
    const registeredCodes = new Set(
      registrations
        .filter((registration) => registration.studentRecordId === studentId)
        .map((registration) => registration.moduleCode)
    )
    return modules.filter((module) => !registeredCodes.has(module.code))
  }, [modules, registrationForm.studentId, registrations])

  useEffect(() => {
    if (
      gradeForm.moduleCode &&
      !gradeModuleOptions.some((module) => module.code === gradeForm.moduleCode)
    ) {
      setGradeForm((previous) => ({ ...previous, moduleCode: '' }))
    }
  }, [gradeForm.moduleCode, gradeModuleOptions])

  useEffect(() => {
    if (
      registrationForm.moduleCode &&
      !registrationModuleOptions.some((module) => module.code === registrationForm.moduleCode)
    ) {
      setRegistrationForm((previous) => ({ ...previous, moduleCode: '' }))
    }
  }, [registrationForm.moduleCode, registrationModuleOptions])

  return (
    <div className="app">
      <header className="page-header grid-span-full">
        <div>
          <p className="eyebrow">Student Gradebook</p>
          <h1>Student grade management dashboard</h1>
          <p className="lede">
            Track academic progress, capture new records, and monitor cohort performance in one place.
          </p>
        </div>
        <div className="user-switcher">
          <p className="eyebrow">Active account</p>
          <div className="user-switcher__controls">
            <select
              value={activeUser.username}
              onChange={handleUserChange}
              aria-label="Choose a demo user"
            >
              {demoUsers.map((user) => (
                <option key={user.username} value={user.username}>
                  {user.label}
                </option>
              ))}
            </select>
            <span className={`role-pill role-${activeUser.role.toLowerCase()}`}>
              {activeUser.role}
            </span>
          </div>
          <p className="helper-text">
            Admin manages everything, teachers add grades, students can only view their record.
          </p>
          <button
            className="ghost-button"
            type="button"
            onClick={() => refreshAll()}
            disabled={isLoading}
          >
            {isLoading ? 'Refreshing...' : 'Refresh data'}
          </button>
        </div>
      </header>

      {loadError && (
        <div className="callout error grid-span-full">Failed to fetch data: {loadError}</div>
      )}
      {authError && !loadError && (
        <div className="callout error grid-span-full">Authentication issue: {authError}</div>
      )}

      {canManage && (
        <section className="card">
          <div>
            <h2>Add a student</h2>
            <p className="section-description">
              Capture new student records so they can enroll in modules and receive grades.
            </p>
          </div>
          <form className="grade-form" onSubmit={handleStudentSubmit}>
            <label>
              First name
              <input
                type="text"
                name="firstName"
                value={studentForm.firstName}
                onChange={handleStudentInputChange}
                required
              />
            </label>
            <label>
              Last name
              <input
                type="text"
                name="lastName"
                value={studentForm.lastName}
                onChange={handleStudentInputChange}
                required
              />
            </label>
            <label>
              Username
              <input
                type="text"
                name="username"
                value={studentForm.username}
                onChange={handleStudentInputChange}
                required
              />
            </label>
            <label>
              Email
              <input
                type="email"
                name="email"
                value={studentForm.email}
                onChange={handleStudentInputChange}
                required
              />
            </label>
            <button
              type="submit"
              className="submit-button"
              disabled={isStudentSubmitting || isLoading}
            >
              {isStudentSubmitting ? 'Saving...' : 'Add student'}
            </button>
            {studentFormStatus.type === 'error' && (
              <p className="form-status error">{studentFormStatus.message}</p>
            )}
            {studentFormStatus.type === 'success' && (
              <p className="form-status success">{studentFormStatus.message}</p>
            )}
          </form>
        </section>
      )}

      {canManage && (
        <section className="card">
          <div>
            <h2>Add a module</h2>
            <p className="section-description">
              Define modules that will appear in the grade picker below.
            </p>
          </div>
          <form className="grade-form" onSubmit={handleModuleSubmit}>
            <label>
              Module code
              <input
                type="text"
                name="code"
                value={moduleForm.code}
                onChange={handleModuleInputChange}
                required
              />
            </label>
            <label>
              Module name
              <input
                type="text"
                name="name"
                value={moduleForm.name}
                onChange={handleModuleInputChange}
                required
              />
            </label>
            <label className="checkbox-field">
              <input
                type="checkbox"
                name="mnc"
                checked={moduleForm.mnc}
                onChange={handleModuleInputChange}
              />
              Marks non-compulsory (MNC)
            </label>
            <button
              type="submit"
              className="submit-button"
              disabled={isModuleSubmitting || isLoading}
            >
              {isModuleSubmitting ? 'Saving...' : 'Add module'}
            </button>
            {moduleFormStatus.type === 'error' && (
              <p className="form-status error">{moduleFormStatus.message}</p>
            )}
            {moduleFormStatus.type === 'success' && (
              <p className="form-status success">{moduleFormStatus.message}</p>
            )}
          </form>
        </section>
      )}

      {canManage && (
        <section className="card">
          <div>
            <h2>Register a student to a module</h2>
            <p className="section-description">
              Link students to the modules they are taking.
            </p>
          </div>
          <form className="grade-form" onSubmit={handleRegistrationSubmit}>
            <label>
              Student
              <select
                name="studentId"
                value={registrationForm.studentId}
                onChange={handleRegistrationInputChange}
                required
              >
                <option value="">Select a student</option>
                {students.map((student) => (
                  <option key={student.id} value={student.id}>
                    {student.firstName} {student.lastName} ({student.username})
                  </option>
                ))}
              </select>
            </label>
            <label>
              Module
              <select
                name="moduleCode"
                value={registrationForm.moduleCode}
                onChange={handleRegistrationInputChange}
                required
                disabled={!registrationForm.studentId || registrationModuleOptions.length === 0}
              >
                <option value="">
                  {!registrationForm.studentId
                    ? 'Select a student first'
                    : registrationModuleOptions.length === 0
                    ? 'No available modules'
                    : 'Select a module'}
                </option>
                {registrationModuleOptions.map((module) => (
                  <option key={module.code} value={module.code}>
                    {module.code} - {module.name}
                  </option>
                ))}
              </select>
            </label>
            {registrationForm.studentId && registrationModuleOptions.length === 0 && (
              <p className="helper-text">
                This student is already registered for every module. Add a new module first.
              </p>
            )}
            <button
              type="submit"
              className="submit-button"
              disabled={isRegistrationSubmitting || isLoading}
            >
              {isRegistrationSubmitting ? 'Registering...' : 'Register student'}
            </button>
            {registrationFormStatus.type === 'error' && (
              <p className="form-status error">{registrationFormStatus.message}</p>
            )}
            {registrationFormStatus.type === 'success' && (
              <p className="form-status success">{registrationFormStatus.message}</p>
            )}
          </form>
        </section>
      )}

      {canRecordGrades && (
        <section className="card">
          <div>
            <h2>Add a grade</h2>
            <p className="section-description">
              Students must already be registered to the selected module before you can record a score.
            </p>
          </div>
          <form className="grade-form" onSubmit={handleGradeSubmit}>
            <label>
              Student
              <select
                name="studentId"
                value={gradeForm.studentId}
                onChange={handleGradeInputChange}
                required
              >
                <option value="">Select a student</option>
                {students.map((student) => (
                  <option key={student.id} value={student.id}>
                    {student.firstName} {student.lastName} ({student.username})
                  </option>
                ))}
              </select>
            </label>
            <label>
              Module
              <select
                name="moduleCode"
                value={gradeForm.moduleCode}
                onChange={handleGradeInputChange}
                required
                disabled={!gradeForm.studentId || gradeModuleOptions.length === 0}
              >
                <option value="">
                  {!gradeForm.studentId
                    ? 'Select a student first'
                    : gradeModuleOptions.length === 0
                    ? 'No registrations found'
                    : 'Select a module'}
                </option>
                {gradeModuleOptions.map((module) => (
                  <option key={module.code} value={module.code}>
                    {module.code} - {module.name}
                  </option>
                ))}
              </select>
            </label>
            {gradeForm.studentId && gradeModuleOptions.length === 0 && (
              <p className="helper-text">
                Register this student to a module before recording grades.
              </p>
            )}
            <label>
              Score
              <input
                type="number"
                name="score"
                min={0}
                max={100}
                value={gradeForm.score}
                onChange={handleGradeInputChange}
                required
              />
            </label>
            <label>
              Academic year
              <input
                type="text"
                name="academicYear"
                placeholder="2024/25"
                value={gradeForm.academicYear}
                onChange={handleGradeInputChange}
                pattern="[0-9]{4}/[0-9]{2}"
                required
              />
            </label>
            <label>
              Semester
              <select
                name="semester"
                value={gradeForm.semester}
                onChange={handleGradeInputChange}
                required
              >
                <option value="">Select a semester</option>
                {semesterOptions.map((option) => (
                  <option key={option} value={option}>
                    {option}
                  </option>
                ))}
              </select>
            </label>
            <button
              type="submit"
              className="submit-button"
              disabled={isGradeSubmitting || isLoading}
            >
              {isGradeSubmitting ? 'Saving...' : 'Save grade'}
            </button>
            {gradeFormStatus.type === 'error' && (
              <p className="form-status error">{gradeFormStatus.message}</p>
            )}
            {gradeFormStatus.type === 'success' && (
              <p className="form-status success">{gradeFormStatus.message}</p>
            )}
          </form>
        </section>
      )}

      <section className="card grid-span-2">
        <div className="card-header">
          <div>
            <h2>{usingStudentView ? 'My grades' : 'Recorded grades'}</h2>
            <p className="section-description">
              Grades shown here reflect the latest records stored in the system.
            </p>
          </div>
        </div>
        {isLoading ? (
          <p className="muted">Loading grades...</p>
        ) : grades.length === 0 ? (
          <p className="muted">No grades captured yet.</p>
        ) : (
          <div className="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>Student</th>
                  <th>Module</th>
                  <th>Academic year</th>
                  <th>Semester</th>
                  <th className="numeric">Score</th>
                  {canRecordGrades && !usingStudentView && <th></th>}
                </tr>
              </thead>
              <tbody>
                {grades.map((grade) => (
                  <tr key={grade.id}>
                    <td>
                      <strong>
                        {grade.student.firstName} {grade.student.lastName}
                      </strong>
                      <span className="muted">{grade.student.username}</span>
                    </td>
                    <td>
                      <strong>
                        {grade.module.code} - {grade.module.name}
                      </strong>
                      {grade.module.mnc && <span className="pill">MNC</span>}
                    </td>
                    <td>{grade.academicYear}</td>
                    <td>{grade.semester}</td>
                    <td className="numeric">{grade.score}</td>
                    {canRecordGrades && !usingStudentView && (
                      <td className="table-actions">
                        <button
                          type="button"
                          className="icon-button"
                          onClick={() => promptGradeDeletion(grade)}
                          aria-label={`Delete grade for ${grade.student.firstName} ${grade.student.lastName} in ${grade.module.code}`}
                        >
                          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" className="size-6">
                            <path fillRule="evenodd" d="M16.5 4.478v.227a48.816 48.816 0 0 1 3.878.512.75.75 0 1 1-.256 1.478l-.209-.035-1.005 13.07a3 3 0 0 1-2.991 2.77H8.084a3 3 0 0 1-2.991-2.77L4.087 6.66l-.209.035a.75.75 0 0 1-.256-1.478A48.567 48.567 0 0 1 7.5 4.705v-.227c0-1.564 1.213-2.9 2.816-2.951a52.662 52.662 0 0 1 3.369 0c1.603.051 2.815 1.387 2.815 2.951Zm-6.136-1.452a51.196 51.196 0 0 1 3.273 0C14.39 3.05 15 3.684 15 4.478v.113a49.488 49.488 0 0 0-6 0v-.113c0-.794.609-1.428 1.364-1.452Zm-.355 5.945a.75.75 0 1 0-1.5.058l.347 9a.75.75 0 1 0 1.499-.058l-.346-9Zm5.48.058a.75.75 0 1 0-1.498-.058l-.347 9a.75.75 0 0 0 1.5.058l.345-9Z" clipRule="evenodd" />
                          </svg>
                          <span className="sr-only">Delete grade</span>
                        </button>
                      </td>
                    )}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
        {deleteError && canRecordGrades && !usingStudentView && (
          <div className="callout error">Failed to delete grade: {deleteError}</div>
        )}
      </section>

      {gradePendingDeletion && canRecordGrades && !usingStudentView && (
        <div className="modal-backdrop" role="presentation">
          <div className="modal" role="dialog" aria-modal="true" aria-labelledby="deleteModalTitle">
            <h3 id="deleteModalTitle">Remove grade?</h3>
            <p>
              This will permanently delete the grade for {gradePendingDeletion.student.firstName}{' '}
              {gradePendingDeletion.student.lastName} in {gradePendingDeletion.module.code} -
              {gradePendingDeletion.module.name}. This action cannot be undone.
            </p>
            <div className="modal-actions">
              <button
                type="button"
                className="ghost-button"
                onClick={dismissDeleteModal}
                disabled={isDeletingGrade}
              >Cancel</button>
              <button
                type="button"
                className="submit-button danger"
                onClick={handleConfirmDelete}
                disabled={isDeletingGrade}
              >
                {isDeletingGrade ? 'Deleting...' : 'Delete grade'}
              </button>
            </div>
          </div>
        </div>
      )}

      <section className="card">
        <div className="card-header">
          <h2>Module averages</h2>
          <p className="section-description">
            Overview of how each module is performing.
          </p>
        </div>
        {moduleAverages.length === 0 ? (
          <p className="muted">No modules have data yet.</p>
        ) : (
          <ul className="stat-list">
            {moduleAverages.map((item) => (
              <li key={item.code}>
                <p className="label">
                  {item.code} - {item.name}
                </p>
                <p className="value">
                  {item.averageScore === null ? '-' : item.averageScore.toFixed(1)}
                </p>
              </li>
            ))}
          </ul>
        )}
      </section>

      {!usingStudentView && (
        <section className="card">
          <div className="card-header">
            <h2>Student averages</h2>
            <p className="section-description">
              Use this to verify academic standing before adding new grades.
            </p>
          </div>
          {studentAverages.length === 0 ? (
            <p className="muted">No students have grades yet.</p>
          ) : (
            <ul className="stat-list">
              {studentAverages.map((item) => (
                <li key={item.id}>
                  <p className="label">
                    {item.firstName} {item.lastName}
                  </p>
                  <p className="value">
                    {item.averageScore === null ? '-' : item.averageScore.toFixed(1)}
                  </p>
                </li>
              ))}
            </ul>
          )}
        </section>
      )}
    </div>
  )
}

export default App
