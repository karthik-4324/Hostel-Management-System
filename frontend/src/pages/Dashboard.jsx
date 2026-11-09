import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";
import { dashboardAPI, adminAPI } from "../services/api";
import {
  Users,
  Building,
  MessageSquare,
  CreditCard,
  Calendar,
  FileText,
  Download,
  Megaphone,
  ChevronRight,
  Zap,
  BedDouble,
  IndianRupee,
  LogOut,
} from "lucide-react";
import toast from "react-hot-toast";

const themes = {
  blue: {
    bg: "#C9D8F0",
    border: "#7FA3E6",
    icon: "#4A6DB5",
    chevron: "#9DB7EA",
  },
  purple: {
    bg: "#DEC8EE",
    border: "#9C5BCB",
    icon: "#7A2FBE",
    chevron: "#BE98E3",
  },
  green: {
    bg: "#C2E7D6",
    border: "#49B186",
    icon: "#2E9E73",
    chevron: "#84CCAD",
  },
  amber: {
    bg: "#F8E3B8",
    border: "#E9A83A",
    icon: "#E58A12",
    chevron: "#F0BB5E",
  },
};

const formatDate = (value) =>
  value
    ? new Date(value).toLocaleDateString("en-US", {
        month: "short",
        day: "2-digit",
        year: "numeric",
      })
    : "";

const StatCard = ({ title, value, icon: Icon, theme, onClick, small }) => (
  <div
    onClick={onClick}
    style={{ backgroundColor: theme.bg, borderColor: theme.border }}
    className={`flex items-center rounded-2xl border-2 p-5 shadow-sm transition-shadow ${
      onClick ? "cursor-pointer hover:shadow-md" : ""
    }`}
  >
    <span
      style={{ backgroundColor: theme.icon }}
      className="flex h-14 w-14 flex-shrink-0 items-center justify-center rounded-full text-white"
    >
      <Icon className="h-7 w-7" />
    </span>
    <span className="ml-4 min-w-0 flex-1">
      <span className="block text-sm text-gray-600">{title}</span>
      <span
        className={`block truncate font-bold text-[#0B1B45] ${
          small ? "text-xl" : "text-3xl"
        }`}
      >
        {value}
      </span>
    </span>
    {onClick && (
      <span
        style={{ backgroundColor: theme.chevron }}
        className="flex h-8 w-8 flex-shrink-0 items-center justify-center rounded-full text-white"
      >
        <ChevronRight className="h-4 w-4" />
      </span>
    )}
  </div>
);

const ActionButton = ({ icon: Icon, label, onClick, primary }) => (
  <button
    onClick={onClick}
    className={`flex w-full items-center rounded-xl border px-4 py-3 text-sm font-semibold transition-colors ${
      primary
        ? "border-[#1E2F94] bg-[#1E2F94] text-white hover:bg-[#18267A]"
        : "border-gray-300 bg-white text-[#0B1B45] hover:bg-gray-50"
    }`}
  >
    <Icon className="h-5 w-5" />
    <span className="flex-1 text-center">{label}</span>
    <ChevronRight className="h-4 w-4" />
  </button>
);

const Dashboard = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [showReportModal, setShowReportModal] = useState(false);
  const [reportParams, setReportParams] = useState({
    type: "students",
    format: "pdf",
    startDate: "",
    endDate: "",
  });

  useEffect(() => {
    fetchDashboardData();
  }, [user]);

  const fetchDashboardData = async () => {
    try {
      let response;
      if (user.role === "ADMIN") {
        response = await dashboardAPI.getAdminStats();
      } else if (user.role === "WARDEN") {
        response = await dashboardAPI.getWardenDashboard();
      } else {
        response = await dashboardAPI.getStudentDashboard();
      }

      if (response.data.success) {
        setStats(response.data.data);
      }
    } catch (error) {
      console.error("Failed to fetch dashboard data:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleLeaveRoom = async () => {
    if (
      !window.confirm(
        "Leave your room? You can only leave after paying all pending fees.",
      )
    )
      return;
    try {
      await dashboardAPI.leaveRoom();
      toast.success("You have left your room");
      fetchDashboardData();
    } catch (error) {
      toast.error(error.response?.data?.message || "Failed to leave room");
    }
  };

  const handleGenerateReport = async () => {
    try {
      const response = await adminAPI.generateReport(reportParams);
      if (response.data.success) {
        toast.success("Report generated successfully");
        if (response.data.data.url) {
          window.open(response.data.data.url, "_blank");
        }
      }
      setShowReportModal(false);
    } catch (error) {
      toast.error("Failed to generate report");
    }
  };

  const handleReportParamChange = (e) => {
    const { name, value } = e.target;
    setReportParams({ ...reportParams, [name]: value });
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="loading-spinner"></div>
      </div>
    );
  }

  const heading = (title, subtitle) => (
    <div>
      <h1 className="text-4xl font-extrabold text-[#0B1B45]">{title}</h1>
      <p className="mt-1 text-gray-600">{subtitle}</p>
    </div>
  );

  const renderStaffDashboard = () => {
    const isWarden = user.role === "WARDEN";
    const s = isWarden ? stats?.stats : stats;
    const totalStudents =
      typeof s?.students === "number" ? s.students : (s?.students?.total ?? 0);
    const rooms = s?.rooms;
    const occupancy =
      rooms?.occupancyRate ??
      (rooms?.total ? (rooms.occupied / rooms.total) * 100 : 0);

    return (
      <div className="space-y-8">
        <div>
          <h1 className="text-4xl font-extrabold text-[#0B1B45]">
            Welcome back,{" "}
            <span className="text-[#1D40B8]">{user.firstName}</span>!
          </h1>
          <p className="mt-1 text-gray-600">
            {isWarden ? "Warden dashboard" : "Admin dashboard"}
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-5">
          <StatCard
            title="Total Students"
            value={totalStudents}
            icon={Users}
            theme={themes.blue}
            onClick={() => navigate("/students")}
          />
          <StatCard
            title="Room Occupancy"
            value={`${Math.round(occupancy)}%`}
            icon={Building}
            theme={themes.green}
            onClick={() => navigate("/rooms")}
          />
          <StatCard
            title="Pending Complaints"
            value={s?.complaints?.pending ?? 0}
            icon={MessageSquare}
            theme={themes.amber}
            onClick={() => navigate("/complaints")}
          />
          {isWarden ? (
            <StatCard
              title="Pending Leaves"
              value={s?.leaves?.pending ?? 0}
              icon={Calendar}
              theme={themes.purple}
              onClick={() => navigate("/leaves")}
            />
          ) : (
            <StatCard
              title="Pending Revenue"
              value={`₹${Number(s?.fees?.pendingRevenue ?? 0).toLocaleString("en-IN")}`}
              icon={IndianRupee}
              theme={themes.purple}
              onClick={() => navigate("/fees")}
            />
          )}
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-5 gap-6">
          <div className="lg:col-span-3 rounded-2xl bg-white p-6 shadow-sm">
            <div className="mb-4 flex items-center">
              <Megaphone className="mr-3 h-6 w-6 text-[#0B1B45]" />
              <h3 className="text-lg font-bold text-[#0B1B45]">
                Recent Activity
              </h3>
            </div>
            <p className="text-gray-500">No recent activity to display.</p>
          </div>

          <div className="lg:col-span-2 rounded-2xl bg-white p-6 shadow-sm">
            <div className="mb-4 flex items-center">
              <Zap className="mr-3 h-6 w-6 text-[#1D40B8]" />
              <h3 className="text-lg font-bold text-[#0B1B45]">
                Quick Actions
              </h3>
            </div>
            <div className="space-y-3">
              {user.role === "ADMIN" ? (
                <>
                  <ActionButton
                    primary
                    icon={Users}
                    label="Manage Students"
                    onClick={() => navigate("/students")}
                  />
                  <ActionButton
                    icon={FileText}
                    label="Generate Report"
                    onClick={() => setShowReportModal(true)}
                  />
                  <ActionButton
                    icon={Megaphone}
                    label="Send Announcement"
                    onClick={() => navigate("/announcements")}
                  />
                </>
              ) : (
                <>
                  <ActionButton
                    primary
                    icon={Users}
                    label="View Students"
                    onClick={() => navigate("/students")}
                  />
                  <ActionButton
                    icon={MessageSquare}
                    label="View Complaints"
                    onClick={() => navigate("/complaints")}
                  />
                  <ActionButton
                    icon={Calendar}
                    label="View Leave Requests"
                    onClick={() => navigate("/leaves")}
                  />
                </>
              )}
            </div>
          </div>
        </div>
      </div>
    );
  };

  const renderStudentDashboard = () => {
    const roomNumber = stats?.student?.roomNumber;
    const announcements = (stats?.recentAnnouncements || []).slice(0, 3);

    return (
      <div className="space-y-8">
        <div>
          <h1 className="text-4xl font-extrabold text-[#0B1B45]">
            Welcome back,{" "}
            <span className="text-[#1D40B8]">{user.firstName}</span>!
          </h1>
          <p className="mt-1 text-gray-600">
            Manage your hostel activities with ease
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-5">
          <StatCard
            title="My Complaints"
            value={stats?.stats?.complaints?.pending ?? 0}
            icon={MessageSquare}
            theme={themes.blue}
            onClick={() => navigate("/complaints")}
          />
          <StatCard
            title="Pending Fees"
            value={`₹${Number(stats?.stats?.fees?.pendingAmount ?? 0).toLocaleString("en-IN")}`}
            icon={IndianRupee}
            theme={themes.purple}
            onClick={() => navigate("/fees")}
          />
          <StatCard
            title="Active Leaves"
            value={stats?.stats?.leaves?.active ?? 0}
            icon={Calendar}
            theme={themes.green}
            onClick={() => navigate("/leaves")}
          />
          <StatCard
            title="Room"
            value={roomNumber || "Not Assigned"}
            icon={BedDouble}
            theme={themes.amber}
            small
          />
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-5 gap-6">
          <div className="lg:col-span-3 rounded-2xl bg-white p-6 shadow-sm">
            <div className="mb-4 flex items-center justify-between">
              <div className="flex items-center">
                <Megaphone className="mr-3 h-6 w-6 text-[#0B1B45]" />
                <h3 className="text-lg font-bold text-[#0B1B45]">
                  Recent Announcements
                </h3>
              </div>
              <button
                onClick={() => navigate("/announcements")}
                className="flex items-center text-sm font-semibold text-[#1D40B8] hover:underline"
              >
                View All
                <ChevronRight className="ml-1 h-4 w-4" />
              </button>
            </div>

            {announcements.length > 0 ? (
              <div className="divide-y divide-gray-100">
                {announcements.map((a) => (
                  <div key={a.id} className="flex items-center py-4">
                    <div className="mr-4 h-10 w-1 flex-shrink-0 rounded-full bg-[#0B1B45]" />
                    <div className="min-w-0 flex-1">
                      <p className="truncate font-semibold text-[#0B1B45]">
                        {a.title}
                      </p>
                      <p className="truncate text-sm text-gray-500">
                        {a.content}
                      </p>
                    </div>
                    <span className="ml-4 flex flex-shrink-0 items-center rounded-full bg-[#0F1F4B] px-3 py-1.5 text-xs font-medium text-white">
                      <Calendar className="mr-1.5 h-3.5 w-3.5" />
                      {formatDate(a.publishDate)}
                    </span>
                  </div>
                ))}
              </div>
            ) : (
              <p className="text-gray-500">No recent announcements.</p>
            )}
          </div>

          <div className="lg:col-span-2 rounded-2xl bg-white p-6 shadow-sm">
            <div className="mb-4 flex items-center">
              <Zap className="mr-3 h-6 w-6 text-[#1D40B8]" />
              <h3 className="text-lg font-bold text-[#0B1B45]">
                Quick Actions
              </h3>
            </div>
            <div className="space-y-3">
              <ActionButton
                primary
                icon={MessageSquare}
                label="Submit Complaint"
                onClick={() => navigate("/complaints")}
              />
              <ActionButton
                icon={Calendar}
                label="Apply for Leave"
                onClick={() => navigate("/leaves")}
              />
              <ActionButton
                icon={CreditCard}
                label="View Fee Details"
                onClick={() => navigate("/fees")}
              />
              {roomNumber && (
                <ActionButton
                  icon={LogOut}
                  label="Leave Room"
                  onClick={handleLeaveRoom}
                />
              )}
            </div>
          </div>
        </div>
      </div>
    );
  };

  return (
    <div className="max-w-7xl mx-auto">
      {user.role === "STUDENT"
        ? renderStudentDashboard()
        : renderStaffDashboard()}

      {/* Report Generation Modal */}
      {showReportModal && (
        <div className="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
          <div className="relative top-20 mx-auto p-5 border w-96 shadow-lg rounded-md bg-white">
            <div className="mt-3">
              <h3 className="text-lg font-medium text-gray-900 mb-4">
                Generate Report
              </h3>
              <div className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700">
                    Report Type
                  </label>
                  <select
                    name="type"
                    value={reportParams.type}
                    onChange={handleReportParamChange}
                    className="input mt-1"
                  >
                    <option value="students">Students Report</option>
                    <option value="fees">Fees Report</option>
                    <option value="complaints">Complaints Report</option>
                    <option value="rooms">Rooms Report</option>
                    <option value="leaves">Leaves Report</option>
                  </select>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700">
                    Format
                  </label>
                  <select
                    name="format"
                    value={reportParams.format}
                    onChange={handleReportParamChange}
                    className="input mt-1"
                  >
                    <option value="pdf">PDF</option>
                    <option value="excel">Excel</option>
                  </select>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700">
                    Start Date (Optional)
                  </label>
                  <input
                    type="date"
                    name="startDate"
                    value={reportParams.startDate}
                    onChange={handleReportParamChange}
                    className="input mt-1"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700">
                    End Date (Optional)
                  </label>
                  <input
                    type="date"
                    name="endDate"
                    value={reportParams.endDate}
                    onChange={handleReportParamChange}
                    className="input mt-1"
                  />
                </div>
              </div>

              <div className="flex justify-end space-x-3 mt-6">
                <button
                  onClick={() => setShowReportModal(false)}
                  className="btn-outline"
                >
                  Cancel
                </button>
                <button onClick={handleGenerateReport} className="btn-primary">
                  <Download className="h-4 w-4 mr-2" />
                  Generate Report
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default Dashboard;
