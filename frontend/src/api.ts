import type { FaultMode, LabStatus, OrderOutcome, Product, ZipkinSpan } from './types'

const CONFIG = (window as unknown as {
  __LAB_CONFIG__?: { gateway?: string; order?: string; product?: string; zipkin?: string }
}).__LAB_CONFIG__ ?? {}

// 所有业务请求统一走网关；Zipkin 是独立监控系统，直连其自带 CORS 的 API
const GATEWAY = CONFIG.gateway ?? 'http://localhost:8080'
const ZIPKIN = CONFIG.zipkin ?? 'http://localhost:9411'

// ---------- 购物相关 ----------

export async function fetchProducts(): Promise<Product[]> {
  const res = await fetch(`${GATEWAY}/api/products/1`)
  const res2 = await fetch(`${GATEWAY}/api/products/2`)
  const [a, b] = await Promise.all([res.json(), res2.json()])
  return [a.data, b.data].filter(Boolean)
}

export async function placeOrder(
  productId: number,
  quantity: number,
  userId = 1,
): Promise<OrderOutcome> {
  const start = performance.now()
  try {
    const res = await fetch(
      `${GATEWAY}/api/orders?userId=${userId}&productId=${productId}&quantity=${quantity}`,
      { method: 'POST' },
    )
    const body = await res.json()
    const costMs = Math.round(performance.now() - start)
    if (res.ok && body.code === 200) {
      return {
        ok: true,
        result: 'SUCCESS',
        message: `订单 ${body.data.orderId} 创建成功，实付 ¥${body.data.amount}`,
        costMs,
      }
    }
    const message = String(body.message ?? '未知错误')
    return {
      ok: false,
      result: message.includes('限流') ? 'BLOCKED' : 'FALLBACK',
      message,
      costMs,
    }
  } catch (e) {
    return {
      ok: false,
      result: 'FALLBACK',
      message: e instanceof Error ? e.message : String(e),
      costMs: Math.round(performance.now() - start),
    }
  }
}

// ---------- 系统状态（教学用，藏在底部） ----------

export async function fetchStatus(): Promise<LabStatus> {
  const res = await fetch(`${GATEWAY}/lab/status`)
  if (!res.ok) throw new Error(`状态接口返回 ${res.status}`)
  return res.json()
}

export async function fetchTraces(limit = 5): Promise<ZipkinSpan[][]> {
  const res = await fetch(`${ZIPKIN}/api/v2/traces?limit=${limit}`)
  if (!res.ok) throw new Error(`Zipkin 返回 ${res.status}`)
  return res.json()
}

/** 店主演示开关：让商品服务临时故障，用于体验熔断。 */
export async function setFaultMode(mode: FaultMode): Promise<void> {
  const res = await fetch(`${GATEWAY}/admin/fault?mode=${mode}`, { method: 'POST' })
  if (!res.ok) throw new Error(`故障注入返回 ${res.status}`)
}

export async function getFaultMode(): Promise<FaultMode> {
  const res = await fetch(`${GATEWAY}/admin/fault`)
  if (!res.ok) throw new Error(`故障查询返回 ${res.status}`)
  const data = await res.json()
  return data.mode as FaultMode
}
