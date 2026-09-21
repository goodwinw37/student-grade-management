import type { ModuleAverage, StudentAverage } from '../types'
import { request } from './client'

export function getModuleAverages(): Promise<ModuleAverage[]> {
  return request<ModuleAverage[]>('/analytics/module-averages')
}

export function getStudentAverages(): Promise<StudentAverage[]> {
  return request<StudentAverage[]>('/analytics/student-averages')
}
