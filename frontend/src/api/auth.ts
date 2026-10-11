import { post } from './request'

export interface LoginResponse {
  userId: number
  nickname: string
  avatarUrl: string
  accessToken: string
  refreshToken: string
  expiresIn: number
}

export function login(nickname: string, password: string) {
  return post<LoginResponse>('/auth/login', { nickname, password })
}
