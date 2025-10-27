const AUTH_ERROR_CODES = [
  1006, // UNAUTHENTICATED
  1011, // EXPIRED_TOKEN
  1012, // INVALID_TOKEN
  1013, // INVALID_REFRESH_TOKEN
  1014, // INVALID_CLIENT
];

const refreshToken = async () => {
  const refreshToken = localStorage.getItem("refresh_token");
  if (!refreshToken) {
    throw new Error("No refresh token found");
  }

  try {
    const response = await fetch("http://localhost:8080/vticket/refresh", {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken: refreshToken }),
    });

    const data = await response.json();

    if (data.code !== 1000) {
      throw new Error(data.desc || "Invalid refresh token");
    }

    const { access_token, refresh_token } = data.result;
    localStorage.setItem("access_token", access_token);
    localStorage.setItem("refresh_token", refresh_token);

    return access_token;
  } catch (error) {
    console.error("Refresh token failed:", error);
    localStorage.removeItem("access_token");
    localStorage.removeItem("refresh_token");
    window.location.href = "/login";
    throw error;
  }
};


export const fetchWithAuth = async (url, options = {}) => {
  let accessToken = localStorage.getItem("access_token");

  const customOptions = {
    ...options,
    headers: { ...options.headers, 'Authorization': `Bearer ${accessToken}` },
  };

  let response = await fetch(url, customOptions);

  // Clone response để có thể đọc body nhiều lần mà không làm mất nó
  const clonedResponse = response.clone();
  let needsRefresh = false;

  if (response.status === 401) {
    // Trường hợp chuẩn: Server trả về 401
    needsRefresh = true;
  } else if (response.ok) {
    // Trường hợp của bạn: Server trả về 200 nhưng code bên trong là lỗi
    try {
      const data = await clonedResponse.json();
      if (data && AUTH_ERROR_CODES.includes(data.code)) {
        console.log(`Authentication error code detected: ${data.code}. Refreshing token...`);
        needsRefresh = true;
      }
    } catch (e) {
    }
  }

  // Nếu phát hiện cần refresh token (từ status 401 hoặc từ code trong body)
  if (needsRefresh) {
    try {
      const newAccessToken = await refreshToken();
      
      // Cập nhật header với token mới và thử lại request ban đầu
      customOptions.headers['Authorization'] = `Bearer ${newAccessToken}`;
      
      console.log("Retrying original request with new token...");
      response = await fetch(url, customOptions); // Gán lại response bằng kết quả của lần gọi lại
    } catch (error) {
      throw new Error("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.");
    }
  }

  return response;
};
