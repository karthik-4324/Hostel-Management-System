import { useState, useEffect } from "react";
import { leavesAPI, studentsAPI, wardenAPI } from "../services/api";
import {
  Plus,
  Search,
  Calendar,
  Clock,
  CheckCircle,
  XCircle,
  Trash2,
} from "lucide-react";
import { useAuth } from "../contexts/AuthContext";
import toast from "react-hot-toast";

const todayStr = () => {
  const d = new Date();
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;
};

const emptyForm = {
  leaveType: "",
  startDate: "",
  endDate: "",
  reason: "",
  emergencyContact: { name: "", phone: "", relation: "" },
  destination: { address: "", city: "", state: "", pincode: "" },
};

const Leaves = () => {
  const { user } = useAuth();
  const [leaves, setLeaves] = useState([]);
  const [students, setStudents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState("");
  const [filterStatus, setFilterStatus] = useState("");
  const [filterType, setFilterType] = useState("");
  const [showLeaveForm, setShowLeaveForm] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [formData, setFormData] = useState(emptyForm);

  useEffect(() => {
    fetchLeaves();
  }, [searchTerm, filterStatus, filterType]);

  useEffect(() => {
    if (user.role !== "STUDENT") {
      fetchStudents();
    }
  }, []);

  const fetchLeaves = async () => {
    try {
      const params = {
        search: searchTerm,
        status: filterStatus,
        leaveType: filterType,
        limit: 100,
      };

      const response = await leavesAPI.getAll(params);
      if (response.data.success) {
        setLeaves(response.data.data.leaves);
      }
    } catch (error) {
      toast.error("Failed to fetch leaves");
    } finally {
      setLoading(false);
    }
  };

  const fetchStudents = async () => {
    try {
      const params = { page: 1, limit: 100 };
      const response =
        user.role === "WARDEN"
          ? await wardenAPI.getStudents(params)
          : await studentsAPI.getAll(params);
      if (response.data.success) {
        setStudents(response.data.data.students);
      }
    } catch (error) {
      console.error("Failed to fetch students");
    }
  };

  const findStudent = (id) => students.find((s) => s.id === id);

  const handleApplyLeave = () => {
    setFormData(emptyForm);
    setShowLeaveForm(true);
  };

  const handleFormSubmit = async (e) => {
    e.preventDefault();
    if (submitting) return;
    if (formData.endDate < formData.startDate) {
      toast.error("End date cannot be before the start date");
      return;
    }
    setSubmitting(true);
    try {
      await leavesAPI.create(formData);
      toast.success("Leave application submitted successfully");
      setShowLeaveForm(false);
      fetchLeaves();
    } catch (error) {
      toast.error(
        error.response?.data?.message || "Failed to submit leave application",
      );
    } finally {
      setSubmitting(false);
    }
  };

  const handleStatus = async (id, status) => {
    let comment = "";
    if (status === "REJECTED") {
      comment = window.prompt("Reason for rejecting this leave:");
      if (!comment || !comment.trim()) return;
    }
    try {
      await leavesAPI.updateStatus(id, { status, comment });
      toast.success(
        status === "APPROVED" ? "Leave approved" : "Leave rejected",
      );
      fetchLeaves();
    } catch (error) {
      toast.error(error.response?.data?.message || "Failed to update leave");
    }
  };

  const handleCancel = async (id) => {
    if (!window.confirm("Cancel this leave application?")) return;
    try {
      await leavesAPI.cancel(id, "Cancelled by student");
      toast.success("Leave cancelled");
      fetchLeaves();
    } catch (error) {
      toast.error(error.response?.data?.message || "Failed to cancel leave");
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm("Delete this leave record?")) return;
    try {
      await leavesAPI.delete(id);
      toast.success("Leave deleted");
      fetchLeaves();
    } catch (error) {
      toast.error(error.response?.data?.message || "Failed to delete leave");
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;

    if (name.startsWith("emergencyContact.")) {
      const field = name.split(".")[1];
      setFormData({
        ...formData,
        emergencyContact: { ...formData.emergencyContact, [field]: value },
      });
    } else if (name.startsWith("destination.")) {
      const field = name.split(".")[1];
      setFormData({
        ...formData,
        destination: { ...formData.destination, [field]: value },
      });
    } else {
      setFormData({ ...formData, [name]: value });
    }
  };

  const getStatusColor = (status) => {
    switch (status?.toLowerCase()) {
      case "approved":
        return "bg-green-100 text-green-800";
      case "pending":
        return "bg-yellow-100 text-yellow-800";
      case "rejected":
        return "bg-red-100 text-red-800";
      case "cancelled":
        return "bg-gray-100 text-gray-800";
      default:
        return "bg-gray-100 text-gray-800";
    }
  };

  const getStatusIcon = (status) => {
    switch (status?.toLowerCase()) {
      case "approved":
        return <CheckCircle className="h-5 w-5 text-green-500" />;
      case "pending":
        return <Clock className="h-5 w-5 text-yellow-500" />;
      case "rejected":
        return <XCircle className="h-5 w-5 text-red-500" />;
      case "cancelled":
        return <XCircle className="h-5 w-5 text-gray-500" />;
      default:
        return <Calendar className="h-5 w-5 text-gray-500" />;
    }
  };

  const calculateDuration = (startDate, endDate) => {
    const start = new Date(startDate);
    const end = new Date(endDate);
    const diffTime = Math.abs(end - start);
    return Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1;
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="loading-spinner"></div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Leaves</h1>
          <p className="text-gray-600">
            {user.role === "STUDENT"
              ? "Apply for and track your leaves"
              : "Manage student leave applications"}
          </p>
        </div>
        {user.role === "STUDENT" && (
          <button onClick={handleApplyLeave} className="btn-primary">
            <Plus className="h-4 w-4 mr-2" />
            Apply for Leave
          </button>
        )}
      </div>

      <div className="card">
        <div className="card-content">
          <div className="flex flex-col md:flex-row md:items-center space-y-4 md:space-y-0 md:space-x-4 mb-6">
            <div className="relative flex-1">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-4 w-4" />
              <input
                type="text"
                placeholder="Search leaves..."
                className="input pl-10"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
              />
            </div>
            <select
              className="input w-full md:w-48"
              value={filterStatus}
              onChange={(e) => setFilterStatus(e.target.value)}
            >
              <option value="">All Status</option>
              <option value="pending">Pending</option>
              <option value="approved">Approved</option>
              <option value="rejected">Rejected</option>
              <option value="cancelled">Cancelled</option>
            </select>
            <select
              className="input w-full md:w-48"
              value={filterType}
              onChange={(e) => setFilterType(e.target.value)}
            >
              <option value="">All Types</option>
              <option value="home">Home</option>
              <option value="medical">Medical</option>
              <option value="emergency">Emergency</option>
              <option value="personal">Personal</option>
              <option value="academic">Academic</option>
              <option value="other">Other</option>
            </select>
          </div>

          <div className="space-y-4">
            {leaves.map((leave) => {
              const student = findStudent(leave.studentId);
              const isPending = leave.status === "PENDING";
              return (
                <div
                  key={leave.id}
                  className="border rounded-lg p-4 hover:shadow-md transition-shadow"
                >
                  <div className="flex justify-between items-start mb-3">
                    <div className="flex-1">
                      <div className="flex items-center space-x-3 mb-2">
                        {getStatusIcon(leave.status)}
                        <h3 className="text-lg font-semibold text-gray-900">
                          {leave.leaveType
                            ? leave.leaveType.charAt(0).toUpperCase() +
                              leave.leaveType.slice(1)
                            : "Leave"}{" "}
                          Leave
                        </h3>
                        <span
                          className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getStatusColor(leave.status)}`}
                        >
                          {leave.status}
                        </span>
                      </div>
                      <p className="text-gray-600 mb-2">{leave.reason}</p>
                      {leave.status === "REJECTED" && leave.rejectionReason && (
                        <p className="text-sm text-red-600 mb-2">
                          Rejection reason: {leave.rejectionReason}
                        </p>
                      )}
                      <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm text-gray-500">
                        <div>
                          <span className="font-medium">Leave ID:</span>{" "}
                          {leave.leaveId}
                        </div>
                        <div>
                          <span className="font-medium">Start Date:</span>{" "}
                          {new Date(leave.startDate).toLocaleDateString()}
                        </div>
                        <div>
                          <span className="font-medium">End Date:</span>{" "}
                          {new Date(leave.endDate).toLocaleDateString()}
                        </div>
                        <div>
                          <span className="font-medium">Duration:</span>{" "}
                          {calculateDuration(leave.startDate, leave.endDate)}{" "}
                          days
                        </div>
                      </div>
                    </div>
                  </div>

                  {user.role !== "STUDENT" && (
                    <div className="flex items-center justify-between pt-3 border-t">
                      <div className="text-sm text-gray-600">
                        Applied by:{" "}
                        {student
                          ? `${student.firstName} ${student.lastName} (${student.studentId})`
                          : "Unknown student"}
                        {student?.roomNumber
                          ? ` · Room ${student.roomNumber}`
                          : ""}
                      </div>
                      <div className="flex items-center space-x-2">
                        {isPending && (
                          <>
                            <button
                              onClick={() => handleStatus(leave.id, "APPROVED")}
                              className="btn-primary text-xs"
                            >
                              Approve
                            </button>
                            <button
                              onClick={() => handleStatus(leave.id, "REJECTED")}
                              className="btn-outline text-xs"
                            >
                              Reject
                            </button>
                          </>
                        )}
                        {user.role === "ADMIN" && (
                          <button
                            onClick={() => handleDelete(leave.id)}
                            className="text-red-600 hover:text-red-800"
                            title="Delete leave"
                          >
                            <Trash2 className="h-4 w-4" />
                          </button>
                        )}
                      </div>
                    </div>
                  )}

                  {user.role === "STUDENT" && isPending && (
                    <div className="flex justify-end pt-3 border-t">
                      <button
                        onClick={() => handleCancel(leave.id)}
                        className="btn-outline text-xs"
                      >
                        Cancel Application
                      </button>
                    </div>
                  )}
                </div>
              );
            })}
          </div>

          {leaves.length === 0 && (
            <div className="text-center py-8">
              <Calendar className="mx-auto h-12 w-12 text-gray-400" />
              <h3 className="mt-2 text-sm font-medium text-gray-900">
                No leave applications
              </h3>
              <p className="mt-1 text-sm text-gray-500">
                {user.role === "STUDENT"
                  ? "You haven't applied for any leaves yet."
                  : "No leave applications have been submitted yet."}
              </p>
            </div>
          )}
        </div>
      </div>

      {/* Leave Form Modal */}
      {showLeaveForm && (
        <div className="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
          <div className="relative top-20 mx-auto p-5 border w-11/12 md:w-3/4 lg:w-2/3 shadow-lg rounded-md bg-white">
            <div className="mt-3">
              <h3 className="text-lg font-medium text-gray-900 mb-4">
                Apply for Leave
              </h3>
              <form onSubmit={handleFormSubmit} className="space-y-4">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700">
                      Leave Type
                    </label>
                    <select
                      name="leaveType"
                      value={formData.leaveType}
                      onChange={handleInputChange}
                      className="input mt-1"
                      required
                    >
                      <option value="">Select Leave Type</option>
                      <option value="home">Home</option>
                      <option value="medical">Medical</option>
                      <option value="emergency">Emergency</option>
                      <option value="personal">Personal</option>
                      <option value="academic">Academic</option>
                      <option value="other">Other</option>
                    </select>
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700">
                      Start Date
                    </label>
                    <input
                      type="date"
                      name="startDate"
                      value={formData.startDate}
                      onChange={handleInputChange}
                      min={todayStr()}
                      className="input mt-1"
                      required
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700">
                      End Date
                    </label>
                    <input
                      type="date"
                      name="endDate"
                      value={formData.endDate}
                      onChange={handleInputChange}
                      min={formData.startDate || todayStr()}
                      className="input mt-1"
                      required
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700">
                      Duration
                    </label>
                    <input
                      type="text"
                      value={
                        formData.startDate && formData.endDate
                          ? `${calculateDuration(formData.startDate, formData.endDate)} days`
                          : "Select dates"
                      }
                      className="input mt-1 bg-gray-50"
                      readOnly
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700">
                    Reason
                  </label>
                  <textarea
                    name="reason"
                    value={formData.reason}
                    onChange={handleInputChange}
                    className="input mt-1"
                    rows="3"
                    maxLength={500}
                    required
                    placeholder="Detailed reason for leave"
                  />
                </div>

                <div className="border-t pt-4">
                  <h4 className="text-md font-medium text-gray-900 mb-3">
                    Emergency Contact
                  </h4>
                  <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                    <div>
                      <label className="block text-sm font-medium text-gray-700">
                        Name
                      </label>
                      <input
                        type="text"
                        name="emergencyContact.name"
                        value={formData.emergencyContact.name}
                        onChange={handleInputChange}
                        className="input mt-1"
                        required
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-700">
                        Phone
                      </label>
                      <input
                        type="tel"
                        name="emergencyContact.phone"
                        value={formData.emergencyContact.phone}
                        onChange={handleInputChange}
                        className="input mt-1"
                        required
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-700">
                        Relation
                      </label>
                      <input
                        type="text"
                        name="emergencyContact.relation"
                        value={formData.emergencyContact.relation}
                        onChange={handleInputChange}
                        className="input mt-1"
                        required
                      />
                    </div>
                  </div>
                </div>

                <div className="border-t pt-4">
                  <h4 className="text-md font-medium text-gray-900 mb-3">
                    Destination
                  </h4>
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div className="md:col-span-2">
                      <label className="block text-sm font-medium text-gray-700">
                        Address
                      </label>
                      <input
                        type="text"
                        name="destination.address"
                        value={formData.destination.address}
                        onChange={handleInputChange}
                        className="input mt-1"
                        placeholder="Full address"
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-700">
                        City
                      </label>
                      <input
                        type="text"
                        name="destination.city"
                        value={formData.destination.city}
                        onChange={handleInputChange}
                        className="input mt-1"
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-700">
                        State
                      </label>
                      <input
                        type="text"
                        name="destination.state"
                        value={formData.destination.state}
                        onChange={handleInputChange}
                        className="input mt-1"
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-700">
                        Pincode
                      </label>
                      <input
                        type="text"
                        name="destination.pincode"
                        value={formData.destination.pincode}
                        onChange={handleInputChange}
                        className="input mt-1"
                      />
                    </div>
                  </div>
                </div>

                <div className="flex justify-end space-x-3 pt-4">
                  <button
                    type="button"
                    onClick={() => setShowLeaveForm(false)}
                    className="btn-outline"
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    className="btn-primary"
                    disabled={submitting}
                  >
                    {submitting ? "Submitting..." : "Submit Application"}
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default Leaves;
