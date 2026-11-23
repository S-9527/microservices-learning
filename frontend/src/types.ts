// 后端 /lab/status 与 Zipkin API 的响应类型

export interface Product {
  id: number
  name: string
  price: number
  stock: number
}

export interface CartItem {
  product: Product
  quantity: number
}

export type BreakerState = 'CLOSED' | 'OPEN' | 'HALF_OPEN' | 'UNKNOWN'
export type RequestResult = 'SUCCESS' | 'FALLBACK' | 'BLOCKED'

export interface RecordItem {
  time: number
  result: RequestResult
  detail: string
  costMs: number
}

export interface LabStatus {
  circuitBreaker: BreakerState
  degradeRules: Array<{
    resource: string
    grade: number
    count: number
    timeWindow: number
    minRequestAmount: number
  }>
  flowRules: Array<{ resource: string; grade: number; count: number }>
  stats: {
    total: number
    success: number
    fallback: number
    blocked: number
  }
  recent: RecordItem[]
}

export interface ZipkinSpan {
  id: string
  traceId: string
  parentId?: string
  name: string
  timestamp: number
  duration: number
  localEndpoint?: { serviceName: string }
}

export type FaultMode = 'OFF' | 'ERROR' | 'SLOW'

// 一次下单请求的结果（前端本地统计）
export interface OrderOutcome {
  ok: boolean
  result: RequestResult
  message: string
  costMs: number
}
