/**
 * 业务 API 封装 —— 与接口文档 v1.1.0 对齐
 */
import { get, post } from './request'

export interface PageResult<T> {
  list: T[]
  total: number
  page: number
  size: number
  totalPages: number
}

export interface DefinitionEntry {
  pos: string
  meanings: string[]
}

export interface StarItem {
  id: number
  word: string
  phoneticUk: string
  phoneticUs: string
  definition: DefinitionEntry[]
  frequency: number
  cet6Count: number
  difficulty: number
}

export interface StarDetail extends StarItem {
  exampleEn: string
  exampleCn: string
  affix: string
  synonyms: string[]
  antonyms: string[]
  userStatus: number | null
}

export function starsPage(page: number, size: number, letter?: string, pos?: string) {
  const params = new URLSearchParams({ page: String(page), size: String(size) })
  if (letter) params.set('letter', letter)
  if (pos) params.set('pos', pos)
  return get<PageResult<StarItem>>(`/stars?${params}`)
}

export function starsSearch(q: string, limit = 20) {
  return get<StarItem[]>(`/stars/search?q=${encodeURIComponent(q)}&limit=${limit}`)
}

export function starDetail(id: number) {
  return get<StarDetail>(`/stars/${id}`)
}

/* ---------- 学习轨道 ---------- */

export interface QueueCard {
  wordId: number
  word: string
  phoneticUs: string
  definition: string
  type: 'new' | 'review'
  easeFactor: number
  intervalDays: number
  repetitions: number
}

export interface TodayQueue {
  date: string
  newCount: number
  reviewCount: number
  queue: QueueCard[]
}

export interface ReviewResult {
  wordId: number
  status: number
  repetitions: number
  easeFactor: number
  intervalDays: number
  nextReview: string
  message: string
}

export function todayQueue() {
  return get<TodayQueue>('/orbits/today')
}

export function submitReview(wordId: number, rating: number, mode: 'learn' | 'review') {
  return post<ReviewResult>('/orbits/review', { wordId, rating, mode })
}

export function orbitHistory(wordId: number) {
  return get<unknown[]>(`/orbits/${wordId}/history`)
}

/* ---------- 测验 ---------- */

export interface TestQuestion {
  questionId: string
  type: 'en2cn' | 'cn2en' | 'spelling'
  wordId: number
  stem: string
  options: string[]
}

export interface TestSessionVO {
  sessionId: string
  questions: TestQuestion[]
  total: number
  limitSec: number
}

export interface WrongItem {
  questionId: string
  wordId: number
  stem: string
  yourAnswer: string
  correctAnswer: string
}

export interface SubmitResult {
  sessionId: string
  total: number
  correct: number
  score: number
  wrongQuestions: WrongItem[]
  durationSec: number
  message: string
}

export function createTest(type: 'quiz' | 'exam' | 'quick', count: number) {
  return post<TestSessionVO>('/tests', { type, count })
}

export function submitTest(sessionId: string, answers: { questionId: string; answer: string; costSec: number }[], durationSec: number) {
  return post<SubmitResult>(`/tests/${sessionId}/submit`, { answers, durationSec })
}

/* ---------- 星云统计 ---------- */

export interface NebulaOverview {
  totalLearned: number
  totalMastered: number
  totalReview: number
  remainingNew: number
  streak: number
  todayLearned: number
  todayReviewed: number
  accuracy7d: number | null
  daysToExam: number
}

export interface ConstellationGroup {
  name: string
  total: number
  mastered: number
  stars: { wordId: number; word: string; magnitude: number; mastered: boolean }[]
}

export function nebulaOverview() {
  return get<NebulaOverview>('/nebula/overview')
}

export function nebulaConstellation() {
  return get<{ groups: ConstellationGroup[]; totalStars: number; litStars: number }>('/nebula/constellation')
}
