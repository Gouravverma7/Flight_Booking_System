
import api from "./api";

export async function login(credentials) {
  const response = await api.post("/api/auth/login", {
    email: credentials.email.trim(),
    password: credentials.password,
  });

  const data = response.data;

  if (!data?.accessToken) {
    throw new Error("Login response does not contain an access token.");
  }

  sessionStorage.setItem("accessToken", data.accessToken);

  if (data.refreshToken) {
    sessionStorage.setItem("refreshToken", data.refreshToken);
  }

  if (data.userId != null) {
    sessionStorage.setItem("userId", String(data.userId));
  }

  if (data.name) {
    sessionStorage.setItem("userName", data.name);
  }

  if (data.email) {
    sessionStorage.setItem("userEmail", data.email);
  }

  if (data.role) {
    sessionStorage.setItem("userRole", data.role);
  }

  return data;
}

export function logout() {
  sessionStorage.removeItem("accessToken");
  sessionStorage.removeItem("refreshToken");
  sessionStorage.removeItem("userId");
  sessionStorage.removeItem("userName");
  sessionStorage.removeItem("userEmail");
  sessionStorage.removeItem("userRole");
}


export async function register(userData) {
  const response = await api.post("/api/auth/register", {
    name: userData.name.trim(),
    email: userData.email.trim(),
    password: userData.password,
  });

  return response.data;
}


export async function getMyProfile() {
  const response = await api.get("/api/users/me");
  return response.data;
}

export async function updateMyProfile(profile) {
  const response = await api.patch("/api/users/me", {
    name: profile.name.trim(),
    email: profile.email.trim(),
  });
  return response.data;
}

export async function changeMyPassword(passwordData) {
  await api.patch("/api/users/me/password", {
    currentPassword: passwordData.currentPassword,
    newPassword: passwordData.newPassword,
  });
}


export async function requestPasswordReset(email) {
  const response = await api.post("/api/auth/password/forgot", {
    email: email.trim(),
  });
  return response.data;
}

export async function resetPassword(token, newPassword) {
  const response = await api.post("/api/auth/password/reset", {
    token,
    newPassword,
  });
  return response.data;
}
