import { useCallback, useEffect, useRef, useState } from 'react'
import { ShoppingBag, Wrench, UserRound } from 'lucide-react'
import { toast } from 'sonner'
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'
import { Toaster } from '@/components/ui/sonner'
import {
  fetchProducts,
  fetchStatus,
  fetchTraces,
  getFaultMode,
  placeOrder,
  setFaultMode,
} from './api'
import Cart from './components/Cart'
import ProductList from './components/ProductList'
import SystemBar from './components/SystemBar'
import type { CartItem, FaultMode, LabStatus, Product, ZipkinSpan } from './types'

export default function App() {
  const [products, setProducts] = useState<Product[]>([])
  const [cart, setCart] = useState<CartItem[]>([])
  const [checkingOut, setCheckingOut] = useState(false)
  const [lastOrder, setLastOrder] = useState<string | null>(null)
  const [status, setStatus] = useState<LabStatus | null>(null)
  const [traces, setTraces] = useState<ZipkinSpan[][]>([])
  const [faultMode, setFault] = useState<FaultMode>('OFF')
  const [showDemo, setShowDemo] = useState(false)
  const mounted = useRef(true)

  const loadProducts = useCallback(async () => {
    try {
      const p = await fetchProducts()
      if (mounted.current) setProducts(p)
    } catch { /* 服务未就绪 */ }
  }, [])

  const refresh = useCallback(async () => {
    try {
      const s = await fetchStatus()
      if (mounted.current) setStatus(s)
    } catch { /* 忽略 */ }
    try {
      const t = await fetchTraces()
      if (mounted.current) setTraces(t)
    } catch { /* 忽略 */ }
  }, [])

  useEffect(() => {
    mounted.current = true
    loadProducts()
    refresh()
    getFaultMode().then((m) => mounted.current && setFault(m)).catch(() => {})
    const t = window.setInterval(refresh, 4000)
    return () => {
      mounted.current = false
      window.clearInterval(t)
    }
  }, [loadProducts, refresh])

  const addToCart = (product: Product) => {
    setCart((prev) => {
      const found = prev.find((i) => i.product.id === product.id)
      if (found) {
        return prev.map((i) =>
          i.product.id === product.id
            ? { ...i, quantity: Math.min(i.quantity + 1, product.stock) }
            : i,
        )
      }
      return [...prev, { product, quantity: 1 }]
    })
    toast.success(`已加入 ${product.name}`)
  }

  const changeQty = (productId: number, delta: number) => {
    setCart((prev) =>
      prev
        .map((i) =>
          i.product.id === productId
            ? { ...i, quantity: Math.max(0, Math.min(i.quantity + delta, i.product.stock)) }
            : i,
        )
        .filter((i) => i.quantity > 0),
    )
  }

  const checkout = async () => {
    if (cart.length === 0) return
    setCheckingOut(true)
    let allOk = true
    let message = ''
    for (const item of cart) {
      const outcome = await placeOrder(item.product.id, item.quantity)
      if (!outcome.ok) {
        allOk = false
        message = outcome.message
        break
      }
      message = outcome.message
    }
    setLastOrder(`${allOk ? '✅' : '⚠️'} ${message}`)
    setCheckingOut(false)
    if (allOk) {
      toast.success('下单成功，感谢购买！')
      setCart([])
      loadProducts()
    } else {
      toast.error('下单失败：' + message)
    }
    refresh()
  }

  const changeFault = async (mode: FaultMode) => {
    await setFaultMode(mode)
    setFault(mode)
    toast.info(`商品服务已切到「${mode === 'OFF' ? '正常' : mode === 'ERROR' ? '报错' : '慢响应'}」`)
  }

  return (
    <div className="min-h-screen bg-background pb-16">
      <Toaster richColors position="top-center" />

      <header className="border-b bg-card/60 px-6 py-4">
        <div className="mx-auto flex max-w-7xl items-center justify-between">
          <div className="flex items-center gap-3">
            <span className="flex size-10 items-center justify-center rounded-xl bg-primary text-primary-foreground">
              <ShoppingBag className="size-5" />
            </span>
            <div>
              <h1 className="text-lg font-bold">微服务小商城</h1>
              <p className="text-[11px] text-muted-foreground">下单体验背后的微服务魔法</p>
            </div>
          </div>
          <div className="flex items-center gap-3">
            <Button variant="outline" size="sm" onClick={() => setShowDemo(!showDemo)}>
              <Wrench className="size-4" /> 店主演示
            </Button>
            <span className="flex items-center gap-2 text-xs">
              <span className="flex size-7 items-center justify-center rounded-full bg-primary text-primary-foreground">
                <UserRound className="size-4" />
              </span>
              爱丽丝
            </span>
          </div>
        </div>
      </header>

      {showDemo && (
        <div className="mx-auto max-w-7xl px-6 pt-4">
          <Card className="border-amber-500/40 bg-amber-500/5">
            <CardContent className="flex flex-wrap items-center justify-between gap-3 pt-6">
              <div>
                <div className="text-sm font-semibold text-amber-500">演示开关：让商品服务临时出故障</div>
                <p className="mt-1 text-xs text-muted-foreground">
                  打开后多下几单，看底部状态变「熔断中」。切回正常后等 10 秒，再单独下一单即可恢复。
                </p>
              </div>
              <div className="flex gap-2">
                {(['OFF', 'ERROR', 'SLOW'] as FaultMode[]).map((m) => (
                  <Button
                    key={m}
                    variant={faultMode === m ? 'default' : 'outline'}
                    size="sm"
                    onClick={() => changeFault(m)}
                  >
                    {m === 'OFF' ? '正常' : m === 'ERROR' ? '报错' : '慢响应'}
                  </Button>
                ))}
              </div>
            </CardContent>
          </Card>
        </div>
      )}

      <main className="mx-auto grid max-w-7xl gap-6 px-6 py-8 lg:grid-cols-[1fr_340px]">
        <ProductList products={products} onAdd={addToCart} />
        <Cart
          items={cart}
          onQty={changeQty}
          onRemove={(id) => setCart((prev) => prev.filter((i) => i.product.id !== id))}
          onCheckout={checkout}
          checkingOut={checkingOut}
          lastOrder={lastOrder}
        />
      </main>

      <SystemBar status={status} traces={traces} />
    </div>
  )
}
