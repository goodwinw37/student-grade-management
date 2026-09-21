import type { HalCollection, Module, Registration, Student } from '../types'
import { request } from './client'

export async function getStudents(): Promise<Student[]> {
  return fetchHalCollection<Student>('/students', 'students')
}

export async function getModules(): Promise<Module[]> {
  return fetchHalCollection<Module>('/modules', 'modules')
}

export async function getRegistrations(): Promise<Registration[]> {
  return request<Registration[]>('/management/registrations')
}

export interface CreateStudentPayload {
  firstName: string
  lastName: string
  username: string
  email: string
}

export interface CreateModulePayload {
  code: string
  name: string
  mnc: boolean
}

export interface CreateRegistrationPayload {
  studentId: number
  moduleCode: string
}

export async function createStudent(payload: CreateStudentPayload): Promise<Student> {
  return request<Student>('/management/students', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export async function createModule(payload: CreateModulePayload): Promise<Module> {
  return request<Module>('/management/modules', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export async function createRegistration(
  payload: CreateRegistrationPayload
): Promise<void> {
  await request('/management/registrations', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

async function fetchHalCollection<T>(
  path: string,
  embeddedKey: string
): Promise<T[]> {
  const response = await request<HalCollection<T>>(path)
  return response._embedded?.[embeddedKey] ?? []
}
