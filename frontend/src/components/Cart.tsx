import { Minus, Plus, Trash2, ShoppingBag, CheckCircle2, AlertTriangle, Loader2 } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import {
  Card,
  CardContent,
  CardFooter,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'
import { Separator } from '@/components/ui/separator'
import type { CartItem } from '../types'

interface Props {
  items: CartItem[]
  onQty: (productId: number, delta: number) => void
  onRemove: (productId: number) => void
  onCheckout: () => void
  checkingOut: boolean
  lastOrder: string | null
}

export default function Cart({ items, onQty, onRemove, onCheckout, checkingOut, lastOrder }: Props) {
  const total = items.reduce((sum, i) => sum + i.product.price * i.quantity, 0)
  const count = items.reduce((sum, i) => sum + i.quantity, 0)

  return (
    <Card className="lg:sticky lg:top-6">
      <CardHeader>
        <CardTitle className="flex items-center justify-between">
          <span className="flex items-center gap-2">
            <ShoppingBag className="size-5" /> 购物车
          </span>
          <Badge variant="secondary">{count} 件</Badge>
        </CardTitle>
      </CardHeader>
      <CardContent className="space-y-3">
        {items.length === 0 ? (
          <div className="py-12 text-center text-sm text-muted-foreground">
            购物车是空的，挑点东西吧
          </div>
        ) : (
          items.map(({ product, quantity }) => (
            <div key={product.id} className="flex items-center gap-3 border-b pb-3 last:border-0">
              <div className="min-w-0 flex-1">
                <div className="truncate text-sm font-medium">{product.name}</div>
                <div className="text-xs text-amber-500">¥{product.price}</div>
              </div>
              <div className="flex items-center gap-1">
                <Button
                  variant="ghost"
                  size="icon"
                  className="size-7"
                  onClick={() => onQty(product.id, -1)}
                >
                  <Minus className="size-3" />
                </Button>
                <span className="w-5 text-center font-mono text-sm">{quantity}</span>
                <Button
                  variant="ghost"
                  size="icon"
                  className="size-7"
                  onClick={() => onQty(product.id, 1)}
                  disabled={quantity >= product.stock}
                >
                  <Plus className="size-3" />
                </Button>
                <Button
                  variant="ghost"
                  size="icon"
                  className="size-7 text-muted-foreground hover:text-destructive"
                  onClick={() => onRemove(product.id)}
                >
                  <Trash2 className="size-3" />
                </Button>
              </div>
            </div>
          ))
        )}

        <Separator />

        <div className="flex items-center justify-between">
          <span className="text-sm text-muted-foreground">合计</span>
          <span className="text-2xl font-bold text-amber-500">¥{total}</span>
        </div>

        {lastOrder && (
          <div
            className={`flex items-start gap-2 rounded-md border px-3 py-2 text-xs ${
              lastOrder.startsWith('✅')
                ? 'border-emerald-500/40 bg-emerald-500/10 text-emerald-400'
                : 'border-destructive/40 bg-destructive/10 text-destructive'
            }`}
          >
            {lastOrder.startsWith('✅') ? (
              <CheckCircle2 className="mt-0.5 size-3.5 shrink-0" />
            ) : (
              <AlertTriangle className="mt-0.5 size-3.5 shrink-0" />
            )}
            <span>{lastOrder}</span>
          </div>
        )}
      </CardContent>
      <CardFooter>
        <Button
          onClick={onCheckout}
          disabled={items.length === 0 || checkingOut}
          className="w-full"
          size="lg"
        >
          {checkingOut && <Loader2 className="size-4 animate-spin" />}
          {checkingOut ? '下单中…' : '去结算'}
        </Button>
      </CardFooter>
    </Card>
  )
}
