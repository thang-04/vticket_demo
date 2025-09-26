"use client"

import { useState, useEffect } from "react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import { Badge } from "@/components/ui/badge"
import { Separator } from "@/components/ui/separator"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog"
import { useToast } from "@/hooks/use-toast"
import { Phone, Mail, Calendar, MapPin, Ticket, Settings, LogOut, Edit3, Trash2, Save, X } from "lucide-react"

// Định nghĩa kiểu dữ liệu cho user trả về từ API
interface ApiUserData {
  id: string;
  fullName: string;
  username: string;
  email: string;
  address: string;
  createdAt: string;
  updatedAt: string;
  isActive: boolean;
}

// Định nghĩa kiểu dữ liệu cho response khi update thành công
interface ApiUpdateResponseData {
  id: string;
  full_name: string;
  username: string;
  email: string;
  address: string;
  avatar: string;
  created_at: string;
  updated_at: string;
  isActive: boolean;
  access_token: string;
  refresh_token: string;
  roles: { name: string; description: string }[];
}

// Định nghĩa kiểu dữ liệu cho state của component
interface UserProfileState {
  name: string;
  phone: string; // API không có, giữ lại làm mock
  email: string;
  joinDate: string;
  location: string;
  avatar: string; 
  totalTickets: number; // API không có, giữ lại làm mock
  upcomingEvents: number; // API không có, giữ lại làm mock
}


export function UserProfile() {
  const [user, setUser] = useState<UserProfileState | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const [editDialogOpen, setEditDialogOpen] = useState(false)
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false)
  const [passwordDialogOpen, setPasswordDialogOpen] = useState(false)
  const [isLoading, setIsLoading] = useState(false)
  const [editForm, setEditForm] = useState({
    name: "",
    phone: "",
    email: "",
    location: "",
  })
  const [avatarFile, setAvatarFile] = useState<File | null>(null);
  const [passwordForm, setPasswordForm] = useState({
    currentPassword: "",
    newPassword: "",
    confirmPassword: "",
  })

  const { toast } = useToast()
  
  // Hàm format ngày tháng
  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return `Tháng ${date.getMonth() + 1}, ${date.getFullYear()}`;
  };

  useEffect(() => {
    const fetchUserData = async () => {
      const token = localStorage.getItem("access_token");
      if (!token) {
        setError("Vui lòng đăng nhập để xem thông tin.");
        setLoading(false);
        // window.location.href = "/login";
        return;
      }

      try {
        const response = await fetch("http://localhost:8080/vticket/api/users", {
          headers: {
            "Authorization": `Bearer ${token}`
          }
        });

        if (!response.ok) {
           throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();
        if (data && data.code === 1000 && data.result) {
            const apiUser: ApiUserData & { avatar?: string } = data.result;
            setUser({
                name: apiUser.fullName,
                email: apiUser.email,
                location: apiUser.address,
                joinDate: formatDate(apiUser.createdAt),
                avatar: apiUser.avatar ? `http://localhost:8080${apiUser.avatar}` : "/diverse-user-avatars.png",
                phone: "+84 987 654 321", 
                totalTickets: 12,
                upcomingEvents: 3,
            });
        } else {
            throw new Error(data.desc || "Failed to parse user data.");
        }

      } catch (e: any) {
        console.error("Failed to fetch user data:", e);
        setError(e.message);
      } finally {
        setLoading(false);
      }
    };
    
    fetchUserData();
  }, []);


  const recentTickets = [
    {
      id: 1,
      event: "Sơn Tùng M-TP Concert 2024",
      date: "15/12/2024",
      venue: "Sân vận động Mỹ Đình",
      status: "confirmed",
      price: "1,500,000 VNĐ",
    },
    {
      id: 2,
      event: "Đen Vâu Live Show",
      date: "28/11/2024",
      venue: "Nhà hát Hòa Bình",
      status: "confirmed",
      price: "800,000 VNĐ",
    },
    {
      id: 3,
      event: "Hòa Minzy Fan Meeting",
      date: "05/11/2024",
      venue: "Trung tâm Hội nghị Quốc gia",
      status: "used",
      price: "600,000 VNĐ",
    },
  ]

  const handleUpdateProfile = async () => {
    const token = localStorage.getItem("access_token");
    if (!token) {
        toast({ title: "Lỗi xác thực", description: "Không tìm thấy token. Vui lòng đăng nhập lại.", variant: "destructive" });
        return;
    }
    
    setIsLoading(true)
    try {
      const formData = new FormData();
      
      const nameParts = editForm.name.trim().split(/\s+/);
      const firstName = nameParts.shift() || "";
      const lastName = nameParts.join(" ");

      formData.append("firstName", firstName);
      formData.append("lastName", lastName);
      formData.append("address", editForm.location);

      if (avatarFile) {
        formData.append("avatar", avatarFile);
      }

      const response = await fetch("http://localhost:8080/vticket/api/users/update", {
        method: 'POST',
        headers: {
          "Authorization": `Bearer ${token}`,
        },
        body: formData,
      });

      const data = await response.json();
      
      if (response.ok && data.code === 1000 && data.result) {
        const updatedApiUser: ApiUpdateResponseData = data.result;

        localStorage.setItem("access_token", updatedApiUser.access_token);
        localStorage.setItem("refresh_token", updatedApiUser.refresh_token);

        setUser({
            name: updatedApiUser.full_name,
            email: updatedApiUser.email,
            location: updatedApiUser.address,
            joinDate: formatDate(updatedApiUser.created_at),
            avatar: `http://localhost:8080${updatedApiUser.avatar}`,
            phone: user?.phone || "+84 987 654 321",
            totalTickets: user?.totalTickets || 12,
            upcomingEvents: user?.upcomingEvents || 3,
        });

        setEditDialogOpen(false)
        setAvatarFile(null); // Reset file input
        toast({
          title: "Cập nhật thành công",
          description: "Thông tin cá nhân đã được cập nhật.",
        })
      } else {
        throw new Error(data.desc || "Cập nhật không thành công.");
      }
    } catch (error: any) {
      toast({
        title: "Lỗi cập nhật",
        description: error.message || "Không thể cập nhật thông tin. Vui lòng thử lại.",
        variant: "destructive",
      })
    } finally {
      setIsLoading(false)
    }
  }

  const handleChangePassword = async () => {
    if (passwordForm.newPassword !== passwordForm.confirmPassword) {
      toast({
        title: "Lỗi xác nhận",
        description: "Mật khẩu mới và xác nhận mật khẩu không khớp.",
        variant: "destructive",
      })
      return
    }

    if (passwordForm.newPassword.length < 6) {
      toast({
        title: "Mật khẩu không hợp lệ",
        description: "Mật khẩu mới phải có ít nhất 6 ký tự.",
        variant: "destructive",
      })
      return
    }

    setIsLoading(true)
    try {
      // TODO: Thêm logic gọi API để đổi mật khẩu
      await new Promise((resolve) => setTimeout(resolve, 1500))

      setPasswordDialogOpen(false)
      setPasswordForm({ currentPassword: "", newPassword: "", confirmPassword: "" })
      toast({
        title: "Đổi mật khẩu thành công",
        description: "Mật khẩu của bạn đã được cập nhật.",
      })
    } catch (error) {
      toast({
        title: "Lỗi đổi mật khẩu",
        description: "Không thể đổi mật khẩu. Vui lòng kiểm tra mật khẩu hiện tại.",
        variant: "destructive",
      })
    } finally {
      setIsLoading(false)
    }
  }

  const handleDeleteAccount = async () => {
    setIsLoading(true)
    try {
      // TODO: Thêm logic gọi API để xoá tài khoản
      await new Promise((resolve) => setTimeout(resolve, 2000))

      toast({
        title: "Tài khoản đã được xóa",
        description: "Tài khoản của bạn đã được xóa thành công. Bạn sẽ được chuyển hướng về trang chủ.",
      })

      localStorage.clear(); // Xóa hết token
      setTimeout(() => {
        window.location.href = "/"
      }, 2000)
    } catch (error) {
      toast({
        title: "Lỗi xóa tài khoản",
        description: "Không thể xóa tài khoản. Vui lòng thử lại sau.",
        variant: "destructive",
      })
    } finally {
      setIsLoading(false)
      setDeleteDialogOpen(false)
    }
  }

  const handleLogout = async () => {
    try {
      console.log("[v1] User logout initiated");
      localStorage.removeItem("access_token");
      localStorage.removeItem("refresh_token");
      window.location.href = "/";
    } catch (err) {
      console.error("[v1] Logout error", err);
    }
  };

  const openEditDialog = () => {
    if (!user) return;
    setEditForm({
      name: user.name,
      phone: user.phone,
      email: user.email,
      location: user.location,
    })
    setAvatarFile(null); // Reset file khi mở dialog
    setEditDialogOpen(true)
  }

  if (loading) {
    return (
        <div className="min-h-screen bg-background flex items-center justify-center">
            <p className="text-foreground">Đang tải thông tin người dùng...</p>
        </div>
    );
  }

  if (error) {
      return (
        <div className="min-h-screen bg-background flex items-center justify-center">
            <p className="text-red-500">Lỗi: {error}</p>
        </div>
    );
  }
  
  if (!user) {
     return (
        <div className="min-h-screen bg-background flex items-center justify-center">
            <p className="text-foreground">Không tìm thấy thông tin người dùng.</p>
        </div>
    );
  }


  return (
    <div className="min-h-screen bg-background">
      {/* Header */}
      <div className="border-b border-border bg-card">
        <div className="container mx-auto px-4 py-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 bg-primary rounded flex items-center justify-center font-bold text-white text-lg">
                V
              </div>
              <span className="text-2xl font-bold text-foreground">ticket</span>
            </div>
            <Button
              variant="ghost"
              size="sm"
              onClick={handleLogout}
              className="text-muted-foreground hover:text-foreground"
            >
              <LogOut className="w-4 h-4 mr-2" />
              Đăng xuất
            </Button>
          </div>
        </div>
      </div>

      <div className="container mx-auto px-4 py-8 max-w-4xl">
        <div className="grid gap-6 md:grid-cols-3">
          {/* Profile Card */}
          <div className="md:col-span-1">
            <Card className="bg-card border-border">
              <CardHeader className="text-center">
                <Avatar className="w-24 h-24 mx-auto mb-4">
                  <AvatarImage src={user.avatar || "/placeholder.svg"} alt={user.name} />
                  <AvatarFallback className="bg-primary text-primary-foreground text-2xl">
                    {user.name
                      .split(" ")
                      .map((n) => n[0])
                      .join("")}
                  </AvatarFallback>
                </Avatar>
                <CardTitle className="text-xl text-card-foreground">{user.name}</CardTitle>
                <p className="text-muted-foreground">Thành viên từ {user.joinDate}</p>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="flex items-center gap-3 text-sm">
                  <Phone className="w-4 h-4 text-muted-foreground" />
                  <span className="text-card-foreground">{user.phone}</span>
                </div>
                <div className="flex items-center gap-3 text-sm">
                  <Mail className="w-4 h-4 text-muted-foreground" />
                  <span className="text-card-foreground">{user.email}</span>
                </div>
                <div className="flex items-center gap-3 text-sm">
                  <MapPin className="w-4 h-4 text-muted-foreground" />
                  <span className="text-card-foreground">{user.location}</span>
                </div>

                <Separator className="bg-border" />

                <div className="grid grid-cols-2 gap-4 text-center">
                  <div>
                    <div className="text-2xl font-bold text-primary">{user.totalTickets}</div>
                    <div className="text-xs text-muted-foreground">Tổng vé</div>
                  </div>
                  <div>
                    <div className="text-2xl font-bold text-secondary">{user.upcomingEvents}</div>
                    <div className="text-xs text-muted-foreground">Sự kiện sắp tới</div>
                  </div>
                </div>

                <div className="space-y-2 pt-4">
                  <Button
                    className="w-full bg-primary hover:bg-primary/90 text-primary-foreground"
                    onClick={openEditDialog}
                  >
                    <Edit3 className="w-4 h-4 mr-2" />
                    Chỉnh sửa thông tin
                  </Button>
                </div>
              </CardContent>
            </Card>
          </div>

          {/* Tickets & Activity */}
          <div className="md:col-span-2 space-y-6">
            {/* Stats Cards */}
            <div className="grid grid-cols-2 gap-4">
              <Card className="bg-card border-border">
                <CardContent className="p-6">
                  <div className="flex items-center gap-4">
                    <div className="w-12 h-12 bg-primary/10 rounded-lg flex items-center justify-center">
                      <Ticket className="w-6 h-6 text-primary" />
                    </div>
                    <div>
                      <div className="text-2xl font-bold text-card-foreground">{user.totalTickets}</div>
                      <div className="text-sm text-muted-foreground">Vé đã mua</div>
                    </div>
                  </div>
                </CardContent>
              </Card>

              <Card className="bg-card border-border">
                <CardContent className="p-6">
                  <div className="flex items-center gap-4">
                    <div className="w-12 h-12 bg-secondary/10 rounded-lg flex items-center justify-center">
                      <Calendar className="w-6 h-6 text-secondary" />
                    </div>
                    <div>
                      <div className="text-2xl font-bold text-card-foreground">{user.upcomingEvents}</div>
                      <div className="text-sm text-muted-foreground">Sự kiện sắp tới</div>
                    </div>
                  </div>
                </CardContent>
              </Card>
            </div>

            {/* Recent Tickets */}
            <Card className="bg-card border-border">
              <CardHeader>
                <CardTitle className="text-card-foreground flex items-center gap-2">
                  <Ticket className="w-5 h-5" />
                  Vé gần đây
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                {recentTickets.map((ticket) => (
                  <div
                    key={ticket.id}
                    className="flex items-center justify-between p-4 rounded-lg bg-muted/50 border border-border"
                  >
                    <div className="flex-1">
                      <h4 className="font-semibold text-card-foreground">{ticket.event}</h4>
                      <div className="flex items-center gap-4 mt-2 text-sm text-muted-foreground">
                        <div className="flex items-center gap-1">
                          <Calendar className="w-3 h-3" />
                          {ticket.date}
                        </div>
                        <div className="flex items-center gap-1">
                          <MapPin className="w-3 h-3" />
                          {ticket.venue}
                        </div>
                      </div>
                    </div>
                    <div className="text-right">
                      <div className="font-semibold text-card-foreground">{ticket.price}</div>
                      <Badge
                        variant={ticket.status === "confirmed" ? "default" : "secondary"}
                        className={ticket.status === "confirmed" ? "bg-primary text-primary-foreground" : ""}
                      >
                        {ticket.status === "confirmed" ? "Đã xác nhận" : "Đã sử dụng"}
                      </Badge>
                    </div>
                  </div>
                ))}
              </CardContent>
            </Card>

            {/* Account Actions */}
            <Card className="bg-card border-border">
              <CardHeader>
                <CardTitle className="text-card-foreground">Bảo mật & Tài khoản</CardTitle>
              </CardHeader>
              <CardContent className="space-y-3">
                <Button
                  variant="outline"
                  className="w-full justify-start border-border text-card-foreground hover:bg-accent bg-transparent"
                  onClick={() => setPasswordDialogOpen(true)}
                >
                  <Settings className="w-4 h-4 mr-2" />
                  Thay đổi mật khẩu
                </Button>
                <Separator className="bg-border" />
                <Button
                  variant="outline"
                  className="w-full justify-start border-destructive text-destructive hover:bg-destructive hover:text-destructive-foreground bg-transparent"
                  onClick={() => setDeleteDialogOpen(true)}
                >
                  <Trash2 className="w-4 h-4 mr-2" />
                  Xóa tài khoản
                </Button>
              </CardContent>
            </Card>
          </div>
        </div>
      </div>

      <Dialog open={editDialogOpen} onOpenChange={setEditDialogOpen}>
        <DialogContent className="bg-card border-border text-card-foreground">
          <DialogHeader>
            <DialogTitle>Chỉnh sửa thông tin cá nhân</DialogTitle>
            <DialogDescription className="text-muted-foreground">
              Cập nhật thông tin cá nhân của bạn tại đây.
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="name">Họ và tên</Label>
              <Input
                id="name"
                value={editForm.name}
                onChange={(e) => setEditForm((prev) => ({ ...prev, name: e.target.value }))}
                className="bg-input border-border text-foreground"
              />
            </div>
             <div className="space-y-2">
              <Label htmlFor="location">Địa chỉ</Label>
              <Input
                id="location"
                value={editForm.location}
                onChange={(e) => setEditForm((prev) => ({ ...prev, location: e.target.value }))}
                className="bg-input border-border text-foreground"
              />
            </div>
             <div className="space-y-2">
              <Label htmlFor="avatar">Ảnh đại diện</Label>
              <Input
                id="avatar"
                type="file"
                accept="image/*"
                onChange={(e) => {
                  if (e.target.files && e.target.files[0]) {
                    setAvatarFile(e.target.files[0]);
                  }
                }}
                className="bg-input border-border text-foreground file:mr-4 file:py-2 file:px-4 file:rounded-full file:border-0 file:text-sm file:font-semibold file:bg-primary file:text-primary-foreground hover:file:bg-primary/90"
              />
            </div>
          </div>
          <DialogFooter>
            <Button
              variant="outline"
              onClick={() => setEditDialogOpen(false)}
              disabled={isLoading}
              className="border-border text-card-foreground hover:bg-accent bg-transparent"
            >
              <X className="w-4 h-4 mr-2" />
              Hủy
            </Button>
            <Button
              onClick={handleUpdateProfile}
              disabled={isLoading}
              className="bg-primary hover:bg-primary/90 text-primary-foreground"
            >
              <Save className="w-4 h-4 mr-2" />
              {isLoading ? "Đang lưu..." : "Lưu thay đổi"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <Dialog open={passwordDialogOpen} onOpenChange={setPasswordDialogOpen}>
        <DialogContent className="bg-card border-border text-card-foreground">
          <DialogHeader>
            <DialogTitle>Thay đổi mật khẩu</DialogTitle>
            <DialogDescription className="text-muted-foreground">
              Nhập mật khẩu hiện tại và mật khẩu mới để thay đổi.
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="currentPassword">Mật khẩu hiện tại</Label>
              <Input
                id="currentPassword"
                type="password"
                value={passwordForm.currentPassword}
                onChange={(e) => setPasswordForm((prev) => ({ ...prev, currentPassword: e.target.value }))}
                className="bg-input border-border text-foreground"
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="newPassword">Mật khẩu mới</Label>
              <Input
                id="newPassword"
                type="password"
                value={passwordForm.newPassword}
                onChange={(e) => setPasswordForm((prev) => ({ ...prev, newPassword: e.target.value }))}
                className="bg-input border-border text-foreground"
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="confirmPassword">Xác nhận mật khẩu mới</Label>
              <Input
                id="confirmPassword"
                type="password"
                value={passwordForm.confirmPassword}
                onChange={(e) => setPasswordForm((prev) => ({ ...prev, confirmPassword: e.target.value }))}
                className="bg-input border-border text-foreground"
              />
            </div>
          </div>
          <DialogFooter>
            <Button
              variant="outline"
              onClick={() => {
                setPasswordDialogOpen(false)
                setPasswordForm({ currentPassword: "", newPassword: "", confirmPassword: "" })
              }}
              disabled={isLoading}
              className="border-border text-card-foreground hover:bg-accent bg-transparent"
            >
              <X className="w-4 h-4 mr-2" />
              Hủy
            </Button>
            <Button
              onClick={handleChangePassword}
              disabled={
                isLoading || !passwordForm.currentPassword || !passwordForm.newPassword || !passwordForm.confirmPassword
              }
              className="bg-primary hover:bg-primary/90 text-primary-foreground"
            >
              <Save className="w-4 h-4 mr-2" />
              {isLoading ? "Đang cập nhật..." : "Đổi mật khẩu"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <AlertDialog open={deleteDialogOpen} onOpenChange={setDeleteDialogOpen}>
        <AlertDialogContent className="bg-card border-border text-card-foreground">
          <AlertDialogHeader>
            <AlertDialogTitle className="text-destructive">Xóa tài khoản</AlertDialogTitle>
            <AlertDialogDescription className="text-muted-foreground">
              Bạn có chắc chắn muốn xóa tài khoản không? Hành động này không thể hoàn tác và tất cả dữ liệu của bạn sẽ
              bị xóa vĩnh viễn.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel
              disabled={isLoading}
              className="border-border text-card-foreground hover:bg-accent bg-transparent"
            >
              Hủy
            </AlertDialogCancel>
            <AlertDialogAction
              onClick={handleDeleteAccount}
              disabled={isLoading}
              className="bg-destructive hover:bg-destructive/90 text-destructive-foreground"
            >
              {isLoading ? "Đang xóa..." : "Xóa tài khoản"}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  )
}

