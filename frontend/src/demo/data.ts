export interface DemoWord {
  word: string
  phonetic: string
  pos: string
  meaning: string
  example: string
  exampleCn: string
  frequency: number
  cet6Count: number
}

export const demoWords: DemoWord[] = [
  { word: 'abundance', phonetic: '/əˈbʌndəns/', pos: 'n.', meaning: '丰富；充裕；大量', example: 'There is an abundance of evidence to support the claim.', exampleCn: '有大量证据支持这一说法。', frequency: 1820, cet6Count: 5 },
  { word: 'diligent', phonetic: '/ˈdɪlɪdʒənt/', pos: 'adj.', meaning: '勤奋的；用功的', example: 'A diligent learner reviews a little every day.', exampleCn: '勤奋的学习者每天都会复习一点。', frequency: 1045, cet6Count: 4 },
  { word: 'ambiguous', phonetic: '/æmˈbɪɡjuəs/', pos: 'adj.', meaning: '含糊的；模棱两可的', example: 'The instructions were ambiguous to many readers.', exampleCn: '许多读者觉得这些说明含糊不清。', frequency: 1190, cet6Count: 6 },
  { word: 'resilient', phonetic: '/rɪˈzɪliənt/', pos: 'adj.', meaning: '有韧性的；能迅速恢复的', example: 'The community proved resilient after the storm.', exampleCn: '风暴过后，社区展现出很强的恢复力。', frequency: 1430, cet6Count: 3 },
]

export interface DemoQuestion {
  id: number
  type: 'choice' | 'spelling'
  prompt: string
  options?: string[]
  answer: string
  word: string
  explanation: string
}

// 仅供离线视觉原型模拟判分。正式 API 创建测验时不返回 answer / answerIndex。
export const demoQuestions: DemoQuestion[] = [
  { id: 1, type: 'choice', prompt: 'abundance 最准确的意思是？', options: ['稀缺', '丰富', '犹豫', '破坏'], answer: '丰富', word: 'abundance', explanation: 'abundance n. 丰富；充裕；大量' },
  { id: 2, type: 'choice', prompt: '“含糊的；模棱两可的”对应哪个词？', options: ['ambiguous', 'abundant', 'accurate', 'apparent'], answer: 'ambiguous', word: 'ambiguous', explanation: 'ambiguous adj. 含糊的；模棱两可的' },
  { id: 3, type: 'spelling', prompt: '请拼写：勤奋的；用功的', answer: 'diligent', word: 'diligent', explanation: 'diligent adj. 勤奋的；用功的' },
  { id: 4, type: 'choice', prompt: 'resilient 在语境中通常表示？', options: ['迟钝的', '临时的', '有韧性的', '谨慎的'], answer: '有韧性的', word: 'resilient', explanation: 'resilient adj. 有韧性的；能迅速恢复的' },
  { id: 5, type: 'choice', prompt: 'There is an ___ of evidence. 选出恰当的词。', options: ['abundance', 'ambiguity', 'absence', 'accuracy'], answer: 'abundance', word: 'abundance', explanation: 'an abundance of 表示“大量的”。' },
]
