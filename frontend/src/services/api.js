import axios from "axios";

const API_BASE_URL = "http://localhost:5000/api";

// Create axios instance
const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
});

// Add token to requests
api.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Handle response errors
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem("token");
      window.location.href = "/login";
    }
    return Promise.reject(error);
  },
);

// Auth API
export const authAPI = {
  login: (credentials) => api.post("/api/auth/login", credentials),
  register: (userData) => api.post("/api/auth/register", userData),
  getProfile: () => api.get("/api/auth/profile"),
  updateProfile: (profileData) => api.put("/api/auth/profile", profileData),
};

// Health API
export const healthAPI = {
  check: () => api.get("/health"),
};

// Students API (Admin/Warden only)
export const studentsAPI = {
  getAll: (params) => api.get("/api/admin/students", { params }),
  create: (studentData) => api.post("/api/admin/students", studentData),
  update: (id, studentData) =>
    api.put(`/api/admin/students/${id}`, studentData),
  delete: (id) => api.delete(`/api/admin/students/${id}`),
  assignRoom: (data) => api.post("/api/admin/assign-room", data),
};

// Warden API
export const wardenAPI = {
  getStudents: (params) => api.get("/api/warden/students", { params }),
  getDashboard: () => api.get("/api/warden/dashboard"),
};

// Admin API
export const adminAPI = {
  getDashboard: () => api.get("/api/admin/dashboard"),
  generateReport: (params) => api.get("/api/admin/reports", { params }),
};

// Rooms API
export const roomsAPI = {
  getAll: (params) => api.get("/api/rooms", { params }),
  getAvailable: () => api.get("/api/rooms/available"),
  getStats: () => api.get("/api/rooms/stats"),
  create: (roomData) => api.post("/api/rooms", roomData),
  update: (id, roomData) => api.put(`/api/rooms/${id}`, roomData),
  delete: (id) => api.delete(`/api/rooms/${id}`),
  assignStudent: (id, data) => api.post(`/api/rooms/${id}/assign`, data),
  removeStudent: (id, data) => api.post(`/api/rooms/${id}/remove`, data),
};

// Complaints API
export const complaintsAPI = {
  getAll: (params) => api.get("/api/complaints", { params }),
  getById: (id) => api.get(`/api/complaints/${id}`),
  create: (complaintData) => api.post("/api/complaints", complaintData),
  update: (id, complaintData) =>
    api.put(`/api/complaints/${id}`, complaintData),
  updateStatus: (id, statusData) =>
    api.put(`/api/complaints/${id}/status`, statusData),
  delete: (id) => api.delete(`/api/complaints/${id}`),
  getStats: () => api.get("/api/complaints/stats"),
};

// Fees API
export const feesAPI = {
  getAll: (params) => api.get("/api/fees", { params }),
  getById: (id) => api.get(`/api/fees/${id}`),
  create: (feeData) => api.post("/api/fees", feeData),
  update: (id, feeData) => api.put(`/api/fees/${id}`, feeData),
  delete: (id) => api.delete(`/api/fees/${id}`),
  addPayment: (id, paymentData) =>
    api.post(`/api/fees/${id}/payment`, paymentData),
  getStats: () => api.get("/api/fees/stats"),
  getDefaulters: () => api.get("/api/fees/defaulters"),
};

// Leaves API
export const leavesAPI = {
  getAll: (params) => api.get("/api/leaves", { params }),
  getById: (id) => api.get(`/api/leaves/${id}`),
  create: (leaveData) => api.post("/api/leaves", leaveData),
  update: (id, leaveData) => api.put(`/api/leaves/${id}`, leaveData),
  updateStatus: (id, statusData) =>
    api.put(`/api/leaves/${id}/status`, statusData),
  delete: (id) => api.delete(`/api/leaves/${id}`),
  cancel: (id, reason) => api.post(`/api/leaves/${id}/cancel`, { reason }),
  getStats: () => api.get("/api/leaves/stats"),
};

// Announcements API
export const announcementsAPI = {
  getAll: (params) => api.get("/api/announcements", { params }),
  getById: (id) => api.get(`/api/announcements/${id}`),
  create: (announcementData) =>
    api.post("/api/announcements", announcementData),
  update: (id, announcementData) =>
    api.put(`/api/announcements/${id}`, announcementData),
  delete: (id) => api.delete(`/api/announcements/${id}`),
  toggleLike: (id) => api.post(`/api/announcements/${id}/like`),
  addComment: (id, comment) =>
    api.post(`/api/announcements/${id}/comment`, { comment }),
  getStats: () => api.get("/api/announcements/stats"),
};

// Dashboard API
export const dashboardAPI = {
  getAdminStats: () => api.get("/api/admin/dashboard"),
  getStudentDashboard: () => api.get("/api/dashboard/student"),
  getWardenDashboard: () => api.get("/api/warden/dashboard"),
};

export default api;
