import { useState } from 'react'
import {
  Activity, ChevronUp, ChevronDown, ExternalLink,
  CircleCheck, OctagonX, Gauge, Zap,
} from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import type { BreakerState, LabStatus, ZipkinSpan } from '../types'

const STATE_META: Record<BreakerState, { text: string; variant: 'secondary' | 'destructive' | 'outline'; icon: typeof Activity }> = {
  CLOSED: { text: '正常', variant: 'secondary', icon: CircleCheck },
  OPEN: { text: '熔断中', variant: 'destructive', icon: OctagonX },
  HALF_OPEN: { text: '探测中', variant: 'outline', icon: Activity },
  UNKNOWN: { text: '未知', variant: 'outline', icon: Activity },
}

interface Props {
  status: LabStatus | null
  traces: ZipkinSpan[][]
}

export default function SystemBar({ status, traces }: Props) {
  const [open, setOpen] = useState(false)
  const s = status?.stats
  const meta = STATE_META[status?.circuitBreaker ?? 'UNKNOWN']
  const StateIcon = meta.icon
  const latest = traces[0]

  return (
    <div className="fixed inset-x-0 bottom-0 z-50 border-t bg-background/95 backdrop-blur">
      <div className="mx-auto flex max-w-7xl items-center justify-between gap-4 px-6 py-2.5">
        <div className="flex flex-wrap items-center gap-4 text-xs">
          <span className="flex items-center gap-1.5 font-semibold">
            <Gauge className="size-3.5 text-primary" /> 系统状态
          </span>
          <Badge variant={meta.variant}>
            <StateIcon className="size-3" /> 服务{meta.text}
          </Badge>
          <span className="text-muted-foreground">成功 <b className="text-emerald-500">{s?.success ?? 0}</b></span>
          <span className="text-muted-foreground">降级 <b className="text-rose-500">{s?.fallback ?? 0}</b></span>
          <span className="text-muted-foreground">限流 <b className="text-amber-500">{s?.blocked ?? 0}</b></span>
          {latest && (
            <span className="hidden items-center gap-1 text-muted-foreground md:flex">
              <Zap className="size-3" /> 最近链路 {latest.length} 个服务 · {latest[0]?.traceId?.slice(0, 8)}
            </span>
          )}
        </div>
        <Button variant="ghost" size="sm" onClick={() => setOpen(!open)}>
          {open ? <ChevronDown className="size-4" /> : <ChevronUp className="size-4" />}
          {open ? '收起' : '这是什么？'}
        </Button>
      </div>

      {open && (
        <div className="mx-auto max-w-7xl px-6 pb-4 text-xs leading-relaxed text-muted-foreground">
          <p>
            这是一个微服务演示商店。每次下单都会经过{' '}
            <b className="text-foreground">网关 → 订单服务 → 用户/商品服务</b>，最后写入真实 MySQL。
            当商品服务出故障时，订单服务会「熔断」保护自己，你的请求会收到友好提示而不是一直转圈。
          </p>
          <div className="mt-2 flex flex-wrap gap-3">
            {[
              { label: 'Sentinel 控制台', url: 'http://localhost:8090' },
              { label: 'Zipkin 链路', url: 'http://localhost:9411' },
              { label: 'Nacos 控制台', url: 'http://localhost:8088' },
            ].map((l) => (
              <a
                key={l.label}
                href={l.url}
                target="_blank"
                rel="noreferrer"
                className="flex items-center gap-1 text-primary hover:underline"
              >
                {l.label} <ExternalLink className="size-3" />
              </a>
            ))}
          </div>
        </div>
      )}
    </div>
  )
}
