"use client"

import type React from "react"
import { useState, useRef, useEffect } from "react"
import { useRouter, useSearchParams } from "next/navigation"
import Image from "next/image"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { useToast } from "@/hooks/use-toast"

export function OtpVerification() {
  const [otp, setOtp] = useState(["", "", "", "", "", ""])
  const [isLoading, setIsLoading] = useState(false)
  const [countdown, setCountdown] = useState(60)
  const [error, setError] = useState("")
  const { toast } = useToast()
  const router = useRouter()
  const searchParams = useSearchParams()

  const email = searchParams.get("email") || ""
  const type = searchParams.get("type") || "login"
  const firstName = searchParams.get("firstName") || ""
  const lastName = searchParams.get("lastName") || ""
  const username = searchParams.get("username") || ""

  const otpRefs = useRef<(HTMLInputElement | null)[]>([])

  useEffect(() => {
    if (!email) {
      router.push("/login")
      return
    }
  }, [email, router])

  useEffect(() => {
    let timer: NodeJS.Timeout
    if (countdown > 0) {
      timer = setTimeout(() => setCountdown(countdown - 1), 1000)
    }
    return () => clearTimeout(timer)
  }, [countdown])

  const handleOtpChange = (index: number, value: string) => {
    if (value.length > 1) return

    const newOtp = [...otp]
    newOtp[index] = value
    setOtp(newOtp)
    setError("")

    // Auto-focus next input
    if (value && index < 5) {
      otpRefs.current[index + 1]?.focus()
    }
  }

  const handleOtpKeyDown = (index: number, e: React.KeyboardEvent) => {
    if (e.key === "Backspace" && !otp[index] && index > 0) {
      otpRefs.current[index - 1]?.focus()
    }
  }

  const handleOtpPaste = (e: React.ClipboardEvent) => {
    e.preventDefault()
    const pastedData = e.clipboardData.getData("text").slice(0, 6)
    const newOtp = [...otp]

    for (let i = 0; i < pastedData.length && i < 6; i++) {
      if (/^\d$/.test(pastedData[i])) {
        newOtp[i] = pastedData[i]
      }
    }

    setOtp(newOtp)
    setError("")
  }

  const handleVerifyOTP = async () => {
    const otpString = otp.join("")
    if (otpString.length !== 6) {
      setError("Vui lòng nhập đầy đủ 6 số OTP")
      return
    }

    setIsLoading(true)
    setError("")

    try {
      await new Promise((resolve) => setTimeout(resolve, 1500))

      // Simulate success/failure
      if (otpString === "123456") {
        if (type === "register") {
          toast({
            title: "Đăng ký thành công",
            description: `Chào mừng ${firstName} ${lastName} đến với Vticket!`,
          })
        } else {
          toast({
            title: "Đăng nhập thành công",
            description: "Chào mừng bạn đến với Vticket!",
          })
        }
        router.push("/dashboard")
      } else {
        setError("Mã OTP không chính xác. Vui lòng thử lại.")
      }
    } catch (error) {
      setError("Có lỗi xảy ra. Vui lòng thử lại.")
    } finally {
      setIsLoading(false)
    }
  }

  const handleResendOTP = async () => {
    if (countdown > 0) return

    setIsLoading(true)
    setError("")

    try {
      await new Promise((resolve) => setTimeout(resolve, 1000))
      setCountdown(60)
      setOtp(["", "", "", "", "", ""])
      toast({
        title: "OTP mới đã được gửi",
        description: `Mã xác thực mới đã được gửi đến email ${email}`,
      })
    } catch (error) {
      setError("Không thể gửi lại OTP. Vui lòng thử lại.")
    } finally {
      setIsLoading(false)
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

      {/* Right side - OTP form */}
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
            <h1 className="text-2xl font-bold text-foreground">Xác thực OTP</h1>
            <p className="text-muted-foreground">Nhập mã OTP được gửi đến email {email}</p>
          </div>

          <div className="space-y-6">
            <div className="space-y-4">
              <div className="flex gap-2 justify-center" onPaste={handleOtpPaste}>
                {otp.map((digit, index) => (
                  <Input
                    key={index}
                    // ref={(el) => (otpRefs.current[index] = el)}
                    type="text"
                    inputMode="numeric"
                    pattern="[0-9]*"
                    maxLength={1}
                    value={digit}
                    onChange={(e) => handleOtpChange(index, e.target.value)}
                    onKeyDown={(e) => handleOtpKeyDown(index, e)}
                    className="w-14 h-14 text-center text-xl font-semibold bg-input border-border text-foreground"
                    disabled={isLoading}
                  />
                ))}
              </div>

              {error && <p className="text-sm text-destructive text-center">{error}</p>}
            </div>

            <div className="flex justify-between items-center text-sm">
              <Button
                variant="ghost"
                onClick={() => router.push(type === "register" ? "/register" : "/login")}
                className="text-muted-foreground hover:text-foreground"
                disabled={isLoading}
              >
                ← Quay lại
              </Button>

              <Button
                variant="ghost"
                onClick={handleResendOTP}
                disabled={countdown > 0 || isLoading}
                className="text-primary hover:text-primary/80 disabled:text-muted-foreground"
              >
                {countdown > 0 ? `Gửi lại sau ${countdown}s` : "Gửi lại OTP"}
              </Button>
            </div>

            <Button
              onClick={handleVerifyOTP}
              className="w-full bg-primary hover:bg-primary/90 text-primary-foreground"
              disabled={otp.join("").length !== 6 || isLoading}
            >
              {isLoading ? "Đang xác thực..." : type === "register" ? "Hoàn tất đăng ký" : "Xác thực và đăng nhập"}
            </Button>
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
