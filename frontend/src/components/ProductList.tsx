import { Keyboard, Armchair, Package, ShoppingCart } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'
import type { Product } from '../types'

const ICON = { 1: Keyboard, 2: Armchair }
const DESC: Record<number, string> = {
  1: 'Cherry MX 轴体 · 全键无冲 · RGB 背光',
  2: '人体工学设计 · 腰背支撑 · 全网布面',
}

interface Props {
  products: Product[]
  onAdd: (p: Product) => void
}

export default function ProductList({ products, onAdd }: Props) {
  return (
    <section>
      <div className="mb-4 flex items-center gap-2">
        <ShoppingCart className="size-5 text-primary" />
        <h2 className="text-lg font-semibold">精选商品</h2>
      </div>
      <div className="grid gap-4 sm:grid-cols-2">
        {products.map((p) => {
          const Icon = ICON[p.id as keyof typeof ICON] ?? Package
          return (
            <Card key={p.id} className="overflow-hidden transition hover:shadow-lg">
              <div className="flex h-36 items-center justify-center bg-muted/50">
                <Icon className="size-16 text-muted-foreground/60" strokeWidth={1.2} />
              </div>
              <CardHeader>
                <div className="flex items-start justify-between">
                  <CardTitle>{p.name}</CardTitle>
                  <Badge variant={p.stock > 0 ? 'secondary' : 'destructive'}>
                    {p.stock > 0 ? `库存 ${p.stock}` : '已售罄'}
                  </Badge>
                </div>
                <CardDescription>{DESC[p.id] ?? '优质好物'}</CardDescription>
              </CardHeader>
              <CardContent>
                <span className="text-2xl font-bold text-amber-500">¥{p.price}</span>
              </CardContent>
              <CardFooter>
                <Button
                  onClick={() => onAdd(p)}
                  disabled={p.stock <= 0}
                  className="w-full"
                >
                  <ShoppingCart className="size-4" />
                  加入购物车
                </Button>
              </CardFooter>
            </Card>
          )
        })}
      </div>
    </section>
  )
}
