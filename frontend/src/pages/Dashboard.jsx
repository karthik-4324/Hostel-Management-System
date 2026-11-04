import { useState, useEffect } from "react";
import { useAuth } from "../contexts/AuthContext";
import { dashboardAPI, adminAPI } from "../services/api";
import {
  Users,
  Building,
  MessageSquare,
  CreditCard,
  Calendar,
  TrendingUp,
  FileText,
  Download,
} from "lucide-react";
import toast from "react-hot-toast";

const Dashboard = () => {
  const { user } = useAuth();
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

  const handleGenerateReport = async () => {
    try {
      const response = await adminAPI.generateReport(reportParams);
      if (response.data.success) {
        toast.success("Report generated successfully");
        // Handle download or display report URL
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
    setReportParams({
      ...reportParams,
      [name]: value,
    });
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="loading-spinner"></div>
      </div>
    );
  }

  const renderAdminDashboard = () => (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Admin Dashboard</h1>
        <p className="text-gray-600">Welcome back, {user.firstName}!</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <div className="card">
          <div className="card-content">
            <div className="flex items-center">
              <div className="flex-shrink-0">
                <Users className="h-8 w-8 text-blue-600" />
              </div>
              <div className="ml-5 w-0 flex-1">
                <dl>
                  <dt className="text-sm font-medium text-gray-500 truncate">
                    Total Students
                  </dt>
                  <dd className="text-lg font-medium text-gray-900">
                    {stats?.students?.total || 0}
                  </dd>
                </dl>
              </div>
            </div>
          </div>
        </div>

        <div className="card">
          <div className="card-content">
            <div className="flex items-center">
              <div className="flex-shrink-0">
                <Building className="h-8 w-8 text-green-600" />
              </div>
              <div className="ml-5 w-0 flex-1">
                <dl>
                  <dt className="text-sm font-medium text-gray-500 truncate">
                    Room Occupancy
                  </dt>
                  <dd className="text-lg font-medium text-gray-900">
                    {stats?.rooms?.occupancyRate || 0}%
                  </dd>
                </dl>
              </div>
            </div>
          </div>
        </div>

        <div className="card">
          <div className="card-content">
            <div className="flex items-center">
              <div className="flex-shrink-0">
                <MessageSquare className="h-8 w-8 text-yellow-600" />
              </div>
              <div className="ml-5 w-0 flex-1">
                <dl>
                  <dt className="text-sm font-medium text-gray-500 truncate">
                    Pending Complaints
                  </dt>
                  <dd className="text-lg font-medium text-gray-900">
                    {stats?.complaints?.pending || 0}
                  </dd>
                </dl>
              </div>
            </div>
          </div>
        </div>

        <div className="card">
          <div className="card-content">
            <div className="flex items-center">
              <div className="flex-shrink-0">
                <CreditCard className="h-8 w-8 text-red-600" />
              </div>
              <div className="ml-5 w-0 flex-1">
                <dl>
                  <dt className="text-sm font-medium text-gray-500 truncate">
                    Pending Revenue
                  </dt>
                  <dd className="text-lg font-medium text-gray-900">
                    ₹{stats?.fees?.pendingRevenue || 0}
                  </dd>
                </dl>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="card">
          <div className="card-header">
            <h3 className="card-title">Recent Activity</h3>
          </div>
          <div className="card-content">
            <p className="text-gray-500">No recent activity to display.</p>
          </div>
        </div>

        <div className="card">
          <div className="card-header">
            <h3 className="card-title">Quick Actions</h3>
          </div>
          <div className="card-content">
            <div className="space-y-3">
              <button
                onClick={() => (window.location.href = "/students")}
                className="btn-primary w-full"
              >
                <Users className="h-4 w-4 mr-2" />
                Manage Students
              </button>
              <button
                onClick={() => setShowReportModal(true)}
                className="btn-outline w-full"
              >
                <FileText className="h-4 w-4 mr-2" />
                Generate Report
              </button>
              <button
                onClick={() => (window.location.href = "/announcements")}
                className="btn-outline w-full"
              >
                <MessageSquare className="h-4 w-4 mr-2" />
                Send Announcement
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );

  const renderStudentDashboard = () => (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Student Dashboard</h1>
        <p className="text-gray-600">Welcome back, {user.firstName}!</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <div className="card">
          <div className="card-content">
            <div className="flex items-center">
              <div className="flex-shrink-0">
                <MessageSquare className="h-8 w-8 text-blue-600" />
              </div>
              <div className="ml-5 w-0 flex-1">
                <dl>
                  <dt className="text-sm font-medium text-gray-500 truncate">
                    My Complaints
                  </dt>
                  <dd className="text-lg font-medium text-gray-900">
                    {stats?.stats?.complaints?.pending || 0}
                  </dd>
                </dl>
              </div>
            </div>
          </div>
        </div>

        <div className="card">
          <div className="card-content">
            <div className="flex items-center">
              <div className="flex-shrink-0">
                <CreditCard className="h-8 w-8 text-green-600" />
              </div>
              <div className="ml-5 w-0 flex-1">
                <dl>
                  <dt className="text-sm font-medium text-gray-500 truncate">
                    Pending Fees
                  </dt>
                  <dd className="text-lg font-medium text-gray-900">
                    ₹{stats?.stats?.fees?.pendingAmount || 0}
                  </dd>
                </dl>
              </div>
            </div>
          </div>
        </div>

        <div className="card">
          <div className="card-content">
            <div className="flex items-center">
              <div className="flex-shrink-0">
                <Calendar className="h-8 w-8 text-yellow-600" />
              </div>
              <div className="ml-5 w-0 flex-1">
                <dl>
                  <dt className="text-sm font-medium text-gray-500 truncate">
                    Active Leaves
                  </dt>
                  <dd className="text-lg font-medium text-gray-900">
                    {stats?.stats?.leaves?.active || 0}
                  </dd>
                </dl>
              </div>
            </div>
          </div>
        </div>

        <div className="card">
          <div className="card-content">
            <div className="flex items-center">
              <div className="flex-shrink-0">
                <Building className="h-8 w-8 text-purple-600" />
              </div>
              <div className="ml-5 w-0 flex-1">
                <dl>
                  <dt className="text-sm font-medium text-gray-500 truncate">
                    Room
                  </dt>
                  <dd className="text-lg font-medium text-gray-900">
                    {stats?.student?.roomNumber || "Not Assigned"}
                  </dd>
                </dl>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="card">
          <div className="card-header">
            <h3 className="card-title">Recent Announcements</h3>
          </div>
          <div className="card-content">
            {stats?.recentAnnouncements?.length > 0 ? (
              <div className="space-y-3">
                {stats.recentAnnouncements.map((announcement) => (
                  <div
                    key={announcement.id}
                    className="border-l-4 border-blue-500 pl-4"
                  >
                    <h4 className="font-medium">{announcement.title}</h4>
                    <p className="text-sm text-gray-600">
                      {announcement.content.substring(0, 100)}...
                    </p>
                  </div>
                ))}
              </div>
            ) : (
              <p className="text-gray-500">No recent announcements.</p>
            )}
          </div>
        </div>

        <div className="card">
          <div className="card-header">
            <h3 className="card-title">Quick Actions</h3>
          </div>
          <div className="card-content">
            <div className="space-y-3">
              <button
                onClick={() => (window.location.href = "/complaints")}
                className="btn-primary w-full"
              >
                <MessageSquare className="h-4 w-4 mr-2" />
                Submit Complaint
              </button>
              <button
                onClick={() => (window.location.href = "/leaves")}
                className="btn-outline w-full"
              >
                <Calendar className="h-4 w-4 mr-2" />
                Apply for Leave
              </button>
              <button
                onClick={() => (window.location.href = "/fees")}
                className="btn-outline w-full"
              >
                <CreditCard className="h-4 w-4 mr-2" />
                View Fee Details
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );

  return (
    <div className="max-w-7xl mx-auto">
      {user.role === "ADMIN" && renderAdminDashboard()}
      {user.role === "WARDEN" && renderAdminDashboard()}
      {user.role === "STUDENT" && renderStudentDashboard()}

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
