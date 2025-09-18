import Link from "next/link"
import { Button } from "@/components/ui/button"

export default function HomePage() {
  return (
    <div className="min-h-screen bg-background flex items-center justify-center">
      <div className="text-center space-y-6">
        <div className="flex items-center justify-center gap-3 mb-8">
          <div className="w-16 h-16 bg-primary rounded flex items-center justify-center font-bold text-white text-2xl">
            V
          </div>
          <span className="text-4xl font-bold text-foreground">ticket</span>
        </div>
        <p className="text-xl text-muted-foreground mb-8">mua vé, mua vui</p>
        <div className="space-y-4">
          <Link href="/login">
            <Button className="w-full bg-primary hover:bg-primary/90 text-primary-foreground">Đăng nhập</Button>
          </Link>
          <Link href="/events">
            <Button variant="outline" className="w-full border-border text-foreground hover:bg-accent bg-transparent">
              Khám phá sự kiện
            </Button>
          </Link>
          <Link href="/dashboard">
            <Button variant="outline" className="w-full border-border text-foreground hover:bg-accent bg-transparent">
              Xem Dashboard (Demo)
            </Button>
          </Link>
        </div>
      </div>
    </div>
  )
}
