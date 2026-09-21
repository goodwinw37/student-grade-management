import type { Grade } from '../types'
import { request } from './client'

export interface AddGradePayload {
  studentId: number
  moduleCode: string
  score: number
  academicYear: string
  semester: string
}

export async function getGrades(): Promise<Grade[]> {
  return request<Grade[]>('/grades')
}

export async function getMyGrades(): Promise<Grade[]> {
  return request<Grade[]>('/grades/my')
}

export async function deleteGrade(id: number): Promise<void> {
  await request(`/grades/${id}`, { method: 'DELETE' })
}

export async function addGrade(payload: AddGradePayload): Promise<Grade> {
  const body = {
    student_id: payload.studentId.toString(),
    module_code: payload.moduleCode,
    score: payload.score.toString(),
    academic_year: payload.academicYear,
    semester: payload.semester,
  }
  return request<Grade>('/grades/addGrade', {
    method: 'POST',
    body: JSON.stringify(body),
  })
}
