export interface Student {
  id: number
  firstName: string
  lastName: string
  username: string
  email: string
}

export interface Module {
  code: string
  name: string
  mnc: boolean
}

export interface Grade {
  id: number
  score: number
  academicYear: string
  semester: string
  student: GradeStudentSummary
  module: GradeModuleSummary
}

export interface GradeStudentSummary {
  id: number
  firstName: string
  lastName: string
  username: string
}

export interface GradeModuleSummary {
  code: string
  name: string
  mnc: boolean
}

export interface Registration {
  id: number
  studentRecordId: number
  username: string
  moduleCode: string
  moduleName: string
}

export interface HalCollection<T> {
  _embedded?: Record<string, T[]>
}

export interface ModuleAverage {
  code: string
  name: string
  averageScore: number | null
}

export interface StudentAverage {
  id: number
  firstName: string
  lastName: string
  averageScore: number | null
}
