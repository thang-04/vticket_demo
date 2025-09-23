"use client"
import { useState } from "react"
import { useRouter } from "next/navigation"
import Image from "next/image"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { useToast } from "@/hooks/use-toast"

interface FormData {
  firstName: string
  lastName: string
  username: string
  email: string
  password: string
  rePassword: string
}

interface FormErrors {
  firstName?: string
  lastName?: string
  username?: string
  email?: string
  password?: string
  rePassword?: string
}

export interface RegisterResponse {
  code: number
  result?: {
    id: string
    email: string
    firstName: string
    lastName: string
  }
  desc: string
}


export function RegisterForm() {
  const [formData, setFormData] = useState<FormData>({
    firstName: "",
    lastName: "",
    username: "",
    email: "",
    password: "",
    rePassword: "",
  })
  const [errors, setErrors] = useState<FormErrors>({})
  const [isLoading, setIsLoading] = useState(false)
  const { toast } = useToast()
  const router = useRouter()

  const validateForm = (): boolean => {
    const newErrors: FormErrors = {}

    // Validate first name
    if (!formData.firstName.trim()) {
      newErrors.firstName = "Vui lòng nhập họ"
    } else if (formData.firstName.trim().length < 2) {
      newErrors.firstName = "Họ phải có ít nhất 2 ký tự"
    }

    // Validate last name
    if (!formData.lastName.trim()) {
      newErrors.lastName = "Vui lòng nhập tên"
    } else if (formData.lastName.trim().length < 2) {
      newErrors.lastName = "Tên phải có ít nhất 2 ký tự"
    }

    // Validate username
    if (!formData.username.trim()) {
      newErrors.username = "Vui lòng nhập tên đăng nhập"
    } else if (formData.username.trim().length < 3) {
      newErrors.username = "Tên đăng nhập phải có ít nhất 3 ký tự"
    } else if (!/^[a-zA-Z0-9_]+$/.test(formData.username)) {
      newErrors.username = "Tên đăng nhập chỉ được chứa chữ cái, số và dấu gạch dưới"
    }

    // Validate email
    if (!formData.email.trim()) {
      newErrors.email = "Vui lòng nhập email"
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      newErrors.email = "Email không hợp lệ"
    }

    // Validate password
    if (!formData.password) {
      newErrors.password = "Vui lòng nhập mật khẩu"
    } else if (formData.password.length < 6) {
      newErrors.password = "Mật khẩu phải có ít nhất 6 ký tự"
    } else if (!/(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/.test(formData.password)) {
      newErrors.password = "Mật khẩu phải chứa ít nhất 1 chữ hoa, 1 chữ thường và 1 số"
    }

    // Validate re-password
    if (!formData.rePassword) {
      newErrors.rePassword = "Vui lòng nhập lại mật khẩu"
    } else if (formData.password !== formData.rePassword) {
      newErrors.rePassword = "Mật khẩu nhập lại không khớp"
    }

    setErrors(newErrors)
    return Object.keys(newErrors).length === 0
  }

  const handleInputChange = (field: keyof FormData, value: string) => {
    setFormData((prev) => ({ ...prev, [field]: value }))
    // Clear error when user starts typing
    if (errors[field]) {
      setErrors((prev) => ({ ...prev, [field]: undefined }))
    }
  }

const handleRegister = async () => {
  if (!validateForm()) return
  setIsLoading(true)

  try {
    const response = await fetch("http://localhost:8080/vticket/api/users", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        firstName: formData.firstName.trim(),
        lastName: formData.lastName.trim(),
        username: formData.username.trim(),
        email: formData.email.trim().toLowerCase(),
        password: formData.password,
      }),
    })

    const data: RegisterResponse = await response.json()

    if (response.ok && data.code === 1000) {
      toast({
        title: "Đăng ký thành công",
        description: `Mã xác thực đã được gửi đến email ${formData.email}`,
      })

      const params = new URLSearchParams({
        email: formData.email,
        type: "register",
        firstName: formData.firstName,
        lastName: formData.lastName,
        username: formData.username,
        userId: data.result?.id || "",
      })
     router.push(`/verify-otp?email=${encodeURIComponent(formData.email)}`)
    } else {
      // Xử lý lỗi trùng email hoặc username
      if (data.code === 1002) {
        if (data.desc?.toLowerCase().includes("email")) {
          setErrors({ email: "Email này đã được sử dụng" })
        } else if (data.desc?.toLowerCase().includes("username")) {
          setErrors({ username: "Tên đăng nhập này đã được sử dụng" })
        } else {
          toast({
            title: "Lỗi đăng ký",
            description: "Email hoặc tên đăng nhập đã được sử dụng",
            variant: "destructive",
          })
        }
      } else {
        toast({
          title: "Lỗi đăng ký",
          description: data.desc || "Không thể tạo tài khoản. Vui lòng thử lại.",
          variant: "destructive",
        })
      }
    }
  } catch (error) {
    console.error("Registration error:", error)
    toast({
      title: "Lỗi kết nối",
      description: "Không thể kết nối đến máy chủ. Vui lòng thử lại.",
      variant: "destructive",
    })
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

      {/* Right side - Register form */}
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
            <h1 className="text-2xl font-bold text-foreground">Tạo tài khoản mới 🎫</h1>
            <p className="text-muted-foreground">Điền thông tin để tạo tài khoản Vticket</p>
          </div>

          <div className="space-y-4">
            {/* First Name & Last Name */}
            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-1">
                <Input
                  type="text"
                  placeholder="Họ"
                  value={formData.firstName}
                  onChange={(e) => handleInputChange("firstName", e.target.value)}
                  className={`bg-input border-border text-foreground placeholder:text-muted-foreground ${
                    errors.firstName ? "border-destructive" : ""
                  }`}
                  disabled={isLoading}
                />
                {errors.firstName && <p className="text-xs text-destructive">{errors.firstName}</p>}
              </div>
              <div className="space-y-1">
                <Input
                  type="text"
                  placeholder="Tên"
                  value={formData.lastName}
                  onChange={(e) => handleInputChange("lastName", e.target.value)}
                  className={`bg-input border-border text-foreground placeholder:text-muted-foreground ${
                    errors.lastName ? "border-destructive" : ""
                  }`}
                  disabled={isLoading}
                />
                {errors.lastName && <p className="text-xs text-destructive">{errors.lastName}</p>}
              </div>
            </div>

            {/* Username */}
            <div className="space-y-1">
              <Input
                type="text"
                placeholder="Tên đăng nhập"
                value={formData.username}
                onChange={(e) => handleInputChange("username", e.target.value)}
                className={`bg-input border-border text-foreground placeholder:text-muted-foreground ${
                  errors.username ? "border-destructive" : ""
                }`}
                disabled={isLoading}
              />
              {errors.username && <p className="text-xs text-destructive">{errors.username}</p>}
            </div>

            {/* Email */}
            <div className="space-y-1">
              <Input
                type="email"
                placeholder="Email"
                value={formData.email}
                onChange={(e) => handleInputChange("email", e.target.value)}
                className={`bg-input border-border text-foreground placeholder:text-muted-foreground ${
                  errors.email ? "border-destructive" : ""
                }`}
                disabled={isLoading}
              />
              {errors.email && <p className="text-xs text-destructive">{errors.email}</p>}
            </div>

            {/* Password */}
            <div className="space-y-1">
              <Input
                type="password"
                placeholder="Mật khẩu"
                value={formData.password}
                onChange={(e) => handleInputChange("password", e.target.value)}
                className={`bg-input border-border text-foreground placeholder:text-muted-foreground ${
                  errors.password ? "border-destructive" : ""
                }`}
                disabled={isLoading}
              />
              {errors.password && <p className="text-xs text-destructive">{errors.password}</p>}
            </div>

            {/* Re-Password */}
            <div className="space-y-1">
              <Input
                type="password"
                placeholder="Nhập lại mật khẩu"
                value={formData.rePassword}
                onChange={(e) => handleInputChange("rePassword", e.target.value)}
                className={`bg-input border-border text-foreground placeholder:text-muted-foreground ${
                  errors.rePassword ? "border-destructive" : ""
                }`}
                disabled={isLoading}
              />
              {errors.rePassword && <p className="text-xs text-destructive">{errors.rePassword}</p>}
            </div>

            <Button
              onClick={handleRegister}
              className="w-full bg-primary hover:bg-primary/90 text-primary-foreground"
              disabled={isLoading}
            >
              {isLoading ? "Đang tạo tài khoản..." : "Tạo tài khoản"}
            </Button>

            <div className="text-center">
              <span className="text-sm text-muted-foreground">Đã có tài khoản? </span>
              <Button
                variant="link"
                className="text-primary hover:text-primary/80 p-0 h-auto font-normal"
                onClick={() => router.push("/login")}
                disabled={isLoading}
              >
                Đăng nhập ngay
              </Button>
            </div>
          </div>

          <div className="text-center text-xs text-muted-foreground">
            Bằng việc tạo tài khoản, tôi đồng ý với{" "}
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
