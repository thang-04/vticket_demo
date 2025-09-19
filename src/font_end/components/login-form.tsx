"use client"
import { useState } from "react"
import { useRouter } from "next/navigation"
import Image from "next/image"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { useToast } from "@/hooks/use-toast"

export function LoginForm() {
  const [email, setEmail] = useState("")
  const [password, setPassword] = useState("")
  const [isLoading, setIsLoading] = useState(false)
  const [socialLoading, setSocialLoading] = useState<string | null>(null)
  const [error, setError] = useState("")
  const { toast } = useToast()
  const router = useRouter()

  const handleLogin = async () => {
    if (!email || !password) {
      setError("Vui lòng nhập đầy đủ email và mật khẩu")
      return
    }

    setIsLoading(true)
    setError("")

    try {
      await new Promise((resolve) => setTimeout(resolve, 1500))

      // Simulate login validation
      const success = Math.random() > 0.3

      if (success) {
        toast({
          title: "Đăng nhập thành công",
          description: "Chào mừng bạn đến với Vticket!",
        })
        router.push("/dashboard")
      } else {
        setError("Email hoặc mật khẩu không chính xác")
      }
    } catch (error) {
      setError("Có lỗi xảy ra khi đăng nhập. Vui lòng thử lại.")
    } finally {
      setIsLoading(false)
    }
  }

  const handleSocialLogin = async (provider: "google" | "facebook" | "apple") => {
    setSocialLoading(provider)
    setError("")

    try {
      // Simulate social login API call
      await new Promise((resolve) => setTimeout(resolve, 2000))

      // Simulate random success/failure for demo
      const success = Math.random() > 0.3

      if (success) {
        toast({
          title: "Đăng nhập thành công",
          description: `Chào mừng bạn đến với Vticket qua ${provider === "google" ? "Google" : provider === "facebook" ? "Facebook" : "Apple"}!`,
        })
        router.push("/dashboard")
      } else {
        setError(
          `Không thể đăng nhập qua ${provider === "google" ? "Google" : provider === "facebook" ? "Facebook" : "Apple"}. Vui lòng thử lại.`,
        )
      }
    } catch (error) {
      setError("Có lỗi xảy ra khi đăng nhập. Vui lòng thử lại.")
    } finally {
      setSocialLoading(null)
    }
  }

  return (
    <div className="min-h-screen flex">
      {/* Left side - Concert image */}
      <div className="hidden lg:flex lg:w-1/2 relative">
        <Image src="/images/concert-bg.jpg" alt="Concert crowd with phones" fill className="object-cover" priority />
        <div className="absolute inset-0 bg-gradient-to-t from-black/60 to-transparent" />
        <div className="absolute bottom-8 left-8 text-white">
          <div className="flex items-center gap-2 mb-2">
            <div className="w-8 h-8 bg-primary rounded flex items-center justify-center font-bold text-white">V</div>
            <span className="text-xl font-bold">ticket</span>
          </div>
          <p className="text-sm opacity-90">mua vé, mua vui</p>
        </div>
      </div>

      {/* Right side - Login form */}
      <div className="w-full lg:w-1/2 flex items-center justify-center p-8">
        <div className="w-full max-w-md space-y-6">
          {/* Mobile logo */}
          <div className="lg:hidden flex items-center justify-center gap-2 mb-8">
            <div className="w-10 h-10 bg-primary rounded flex items-center justify-center font-bold text-white text-lg">
              V
            </div>
            <span className="text-2xl font-bold text-foreground">ticket</span>
          </div>

          <div className="space-y-2">
            <div className="flex items-center gap-2">
              <div className="w-8 h-8 bg-primary rounded flex items-center justify-center font-bold text-white lg:block hidden">
                V
              </div>
              <span className="text-xl font-bold text-foreground lg:block hidden">ticket</span>
            </div>
            <h1 className="text-2xl font-bold text-foreground">Chào mừng bạn 👋</h1>
            <p className="text-muted-foreground">Đăng nhập vào tài khoản Vticket của bạn</p>
          </div>

          <div className="space-y-4">
            <div className="space-y-3">
              <Input
                type="email"
                placeholder="Email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="bg-input border-border text-foreground placeholder:text-muted-foreground"
                disabled={isLoading || socialLoading !== null}
              />
              <Input
                type="password"
                placeholder="Mật khẩu"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="bg-input border-border text-foreground placeholder:text-muted-foreground"
                disabled={isLoading || socialLoading !== null}
              />
              {error && <p className="text-sm text-destructive">{error}</p>}

              <div className="flex justify-end">
                <Button
                  variant="link"
                  className="text-primary hover:text-primary/80 p-0 h-auto text-sm"
                  onClick={() => router.push("/forgot-password")}
                >
                  Quên mật khẩu?
                </Button>
              </div>

              <Button
                onClick={handleLogin}
                className="w-full bg-primary hover:bg-primary/90 text-primary-foreground"
                disabled={!email || !password || isLoading || socialLoading !== null}
              >
                {isLoading ? "Đang đăng nhập..." : "Đăng nhập"}
              </Button>

              <div className="text-center">
                <span className="text-sm text-muted-foreground">Chưa có tài khoản? </span>
                <Button
                  variant="link"
                  className="text-primary hover:text-primary/80 p-0 h-auto font-normal"
                  onClick={() => router.push("/register")}
                  disabled={isLoading || socialLoading !== null}
                >
                  Đăng ký ngay
                </Button>
              </div>
            </div>

            <div className="relative">
              <div className="absolute inset-0 flex items-center">
                <span className="w-full border-t border-border" />
              </div>
              <div className="relative flex justify-center text-xs uppercase">
                <span className="bg-background px-2 text-muted-foreground">Hoặc sử dụng</span>
              </div>
            </div>

            <div className="grid grid-cols-3 gap-3">
              <Button
                variant="outline"
                className="bg-card border-border hover:bg-accent text-card-foreground"
                onClick={() => handleSocialLogin("google")}
                disabled={socialLoading !== null || isLoading}
              >
                {socialLoading === "google" ? (
                  <div className="w-5 h-5 border-2 border-current border-t-transparent rounded-full animate-spin" />
                ) : (
                  <svg className="w-5 h-5" viewBox="0 0 24 24">
                    <path
                      fill="currentColor"
                      d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"
                    />
                    <path
                      fill="currentColor"
                      d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"
                    />
                    <path
                      fill="currentColor"
                      d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"
                    />
                    <path
                      fill="currentColor"
                      d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"
                    />
                  </svg>
                )}
                <span className="ml-2 text-sm">Google</span>
              </Button>

              <Button
                variant="outline"
                className="bg-card border-border hover:bg-accent text-card-foreground"
                onClick={() => handleSocialLogin("facebook")}
                disabled={socialLoading !== null || isLoading}
              >
                {socialLoading === "facebook" ? (
                  <div className="w-5 h-5 border-2 border-current border-t-transparent rounded-full animate-spin" />
                ) : (
                  <svg className="w-5 h-5" viewBox="0 0 24 24">
                    <path
                      fill="currentColor"
                      d="M24 12.073c0-6.627-5.373-12-12-12s-12 5.373-12 12c0 5.99 4.388 10.954 10.125 11.854v-8.385H7.078v-3.47h3.047V9.43c0-3.007 1.792-4.669 4.533-4.669 1.312 0 2.686.235 2.686.235v2.953H15.83c-1.491 0-1.956.925-1.956 1.874v2.25h3.328l-.532 3.47h-2.796v8.385C19.612 23.027 24 18.062 24 12.073z"
                    />
                  </svg>
                )}
                <span className="ml-2 text-sm">Facebook</span>
              </Button>

              <Button
                variant="outline"
                className="bg-card border-border hover:bg-accent text-card-foreground"
                onClick={() => handleSocialLogin("apple")}
                disabled={socialLoading !== null || isLoading}
              >
                {socialLoading === "apple" ? (
                  <div className="w-5 h-5 border-2 border-current border-t-transparent rounded-full animate-spin" />
                ) : (
                  <svg className="w-5 h-5" viewBox="0 0 24 24">
                    <path
                      fill="currentColor"
                      d="M18.71 19.5c-.83 1.24-1.71 2.45-3.05 2.47-1.34.03-1.77-.79-3.29-.79-1.53 0-2 .77-3.27.82-1.31.05-2.3-1.32-3.14-2.53C4.25 17 2.94 12.45 4.7 9.39c.87-1.52 2.43-2.48 4.12-2.51 1.28-.02 2.5.87 3.64 1.98-.09.06-2.17 1.28-2.15 3.81.03 3.02 2.65 4.03 2.68 4.04-.03.07-.42 1.44-1.38 2.83M13 3.5c.73-.83 1.94-1.46 2.94-1.5.13 1.17-.34 2.35-1.04 3.19-.69.85-1.83 1.51-2.95 1.42-.15-1.15.41-2.35 1.05-3.11z"
                    />
                  </svg>
                )}
                <span className="ml-2 text-sm">Apple</span>
              </Button>
            </div>
          </div>

          <div className="text-center text-xs text-muted-foreground">
            Bằng việc tiếp tục, tôi đồng ý với{" "}
            <a href="#" className="text-primary hover:underline">
              Chính sách bảo mật
            </a>{" "}
            &{" "}
            <a href="#" className="text-primary hover:underline">
              Điều khoản sử dụng
            </a>
          </div>
        </div>
      </div>
    </div>
  )
}
