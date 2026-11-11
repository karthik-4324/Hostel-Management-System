import { useState, useEffect } from "react";
import { complaintsAPI, studentsAPI, wardenAPI } from "../services/api";
import {
  Plus,
  Search,
  MessageSquare,
  Clock,
  CheckCircle,
  Trash2,
  Edit,
} from "lucide-react";
import { useAuth } from "../contexts/AuthContext";
import toast from "react-hot-toast";

const emptyForm = {
  title: "",
  description: "",
  category: "",
  priority: "medium",
  location: "",
};

const Complaints = () => {
  const { user } = useAuth();
  const [complaints, setComplaints] = useState([]);
  const [students, setStudents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState("");
  const [filterStatus, setFilterStatus] = useState("");
  const [filterCategory, setFilterCategory] = useState("");
  const [showComplaintForm, setShowComplaintForm] = useState(false);
  const [editingComplaint, setEditingComplaint] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const [formData, setFormData] = useState(emptyForm);

  useEffect(() => {
    fetchComplaints();
  }, []);

  useEffect(() => {
    if (user.role !== "STUDENT") {
      fetchStudents();
    }
  }, []);

  const fetchComplaints = async () => {
    try {
      const response = await complaintsAPI.getAll({ limit: 100 });
      if (response.data.success) {
        setComplaints(response.data.data.complaints);
      }
    } catch (error) {
      toast.error("Failed to fetch complaints");
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

  const filteredComplaints = complaints.filter((c) => {
    if (filterStatus && c.status?.toLowerCase() !== filterStatus) return false;
    if (filterCategory && c.category?.toLowerCase() !== filterCategory)
      return false;
    if (searchTerm.trim()) {
      const s = findStudent(c.reportedBy);
      const text =
        `${c.title} ${c.description} ${c.complaintId} ${s?.firstName ?? ""} ${s?.lastName ?? ""} ${s?.studentId ?? ""}`.toLowerCase();
      if (!text.includes(searchTerm.toLowerCase())) return false;
    }
    return true;
  });

  const handleStatusChange = async (id, status) => {
    try {
      await complaintsAPI.updateStatus(id, { status });
      toast.success("Status updated");
      fetchComplaints();
    } catch (error) {
      toast.error(error.response?.data?.message || "Failed to update status");
    }
  };

  const handleDeleteComplaint = async (id) => {
    if (window.confirm("Delete this complaint?")) {
      try {
        await complaintsAPI.delete(id);
        toast.success("Complaint deleted");
        fetchComplaints();
      } catch (error) {
        toast.error(
          error.response?.data?.message || "Failed to delete complaint",
        );
      }
    }
  };

  const handleSubmitComplaint = () => {
    setEditingComplaint(null);
    setFormData(emptyForm);
    setShowComplaintForm(true);
  };

  const handleEditComplaint = (complaint) => {
    setEditingComplaint(complaint);
    setFormData({
      title: complaint.title || "",
      description: complaint.description || "",
      category: complaint.category || "",
      priority: (complaint.priority || "medium").toLowerCase(),
      location: complaint.location || "",
    });
    setShowComplaintForm(true);
  };

  const handleFormSubmit = async (e) => {
    e.preventDefault();
    if (submitting) return;
    setSubmitting(true);
    try {
      const payload = {
        ...formData,
        priority: formData.priority.toUpperCase(),
      };
      if (editingComplaint) {
        await complaintsAPI.update(editingComplaint.id, payload);
        toast.success("Complaint updated successfully");
      } else {
        await complaintsAPI.create(payload);
        toast.success("Complaint submitted successfully");
      }
      setShowComplaintForm(false);
      fetchComplaints();
    } catch (error) {
      toast.error(
        error.response?.data?.message ||
          (editingComplaint
            ? "Failed to update complaint"
            : "Failed to submit complaint"),
      );
    } finally {
      setSubmitting(false);
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  const getStatusColor = (status) => {
    switch (status?.toLowerCase()) {
      case "pending":
        return "bg-yellow-100 text-yellow-800";
      case "in_progress":
        return "bg-blue-100 text-blue-800";
      case "resolved":
        return "bg-green-100 text-green-800";
      case "closed":
        return "bg-gray-100 text-gray-800";
      case "rejected":
        return "bg-red-100 text-red-800";
      default:
        return "bg-gray-100 text-gray-800";
    }
  };

  const getPriorityColor = (priority) => {
    switch (priority?.toLowerCase()) {
      case "urgent":
        return "bg-red-100 text-red-800";
      case "high":
        return "bg-orange-100 text-orange-800";
      case "medium":
        return "bg-yellow-100 text-yellow-800";
      case "low":
        return "bg-green-100 text-green-800";
      default:
        return "bg-gray-100 text-gray-800";
    }
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
          <h1 className="text-xl font-bold text-slate-800 tracking-tight mb-1">
            Complaints
          </h1>
          <p className="text-gray-600">
            {user.role === "STUDENT"
              ? "Submit and track your complaints"
              : "Manage student complaints"}
          </p>
        </div>
        {user.role === "STUDENT" && (
          <button onClick={handleSubmitComplaint} className="btn-primary">
            <Plus className="h-4 w-4 mr-2" />
            Submit Complaint
          </button>
        )}
      </div>

      <div className="card">
        <div className="card-content">
          <div className="flex flex-col md:flex-row md:items-center space-y-4 md:space-y-0 md:space-x-4 mb-6 mt-6">
            <div className="relative flex-1">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-4 w-4" />
              <input
                type="text"
                placeholder="Search complaints..."
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
              <option value="in_progress">In Progress</option>
              <option value="resolved">Resolved</option>
              <option value="closed">Closed</option>
              <option value="rejected">Rejected</option>
            </select>
            <select
              className="input w-full md:w-48"
              value={filterCategory}
              onChange={(e) => setFilterCategory(e.target.value)}
            >
              <option value="">All Categories</option>
              <option value="plumbing">Plumbing</option>
              <option value="electrical">Electrical</option>
              <option value="cleaning">Cleaning</option>
              <option value="maintenance">Maintenance</option>
              <option value="security">Security</option>
              <option value="food">Food</option>
              <option value="internet">Internet</option>
              <option value="other">Other</option>
            </select>
          </div>

          <div className="space-y-4">
            {filteredComplaints.map((complaint) => {
              const student = findStudent(complaint.reportedBy);
              const status = complaint.status?.toUpperCase();
              return (
                <div
                  key={complaint.id}
                  className="border rounded-lg p-4 hover:shadow-md transition-shadow"
                >
                  <div className="flex justify-between items-start mb-3">
                    <div className="flex-1">
                      <div className="flex items-center space-x-3 mb-2">
                        <h3 className="text-lg font-semibold text-gray-900">
                          {complaint.title}
                        </h3>
                        <span
                          className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getStatusColor(complaint.status)}`}
                        >
                          {complaint.status?.replace("_", " ")}
                        </span>
                        <span
                          className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getPriorityColor(complaint.priority)}`}
                        >
                          {complaint.priority}
                        </span>
                      </div>
                      <p className="text-gray-600 mb-2">
                        {complaint.description}
                      </p>
                      <div className="flex flex-wrap items-center gap-x-4 gap-y-1 text-sm text-gray-500">
                        <span>ID: {complaint.complaintId}</span>
                        <span>Category: {complaint.category}</span>
                        {complaint.location && (
                          <span>Location: {complaint.location}</span>
                        )}
                        <span>
                          Reported:{" "}
                          {new Date(complaint.createdAt).toLocaleDateString()}
                        </span>
                      </div>
                    </div>
                    <div className="flex items-center space-x-2">
                      {status === "PENDING" && (
                        <Clock className="h-5 w-5 text-yellow-500" />
                      )}
                      {status === "IN_PROGRESS" && (
                        <MessageSquare className="h-5 w-5 text-blue-500" />
                      )}
                      {status === "RESOLVED" && (
                        <CheckCircle className="h-5 w-5 text-green-500" />
                      )}
                    </div>
                  </div>

                  {user.role === "STUDENT" && status === "PENDING" && (
                    <div className="flex justify-end space-x-4 pt-3 border-t">
                      <button
                        onClick={() => handleEditComplaint(complaint)}
                        className="flex items-center text-sm text-blue-600 hover:text-blue-800"
                        title="Edit complaint"
                      >
                        <Edit className="h-4 w-4 mr-1" />
                        Edit
                      </button>
                      <button
                        onClick={() => handleDeleteComplaint(complaint.id)}
                        className="flex items-center text-sm text-red-600 hover:text-red-800"
                        title="Delete complaint"
                      >
                        <Trash2 className="h-4 w-4 mr-1" />
                        Delete
                      </button>
                    </div>
                  )}

                  {user.role !== "STUDENT" && (
                    <div className="flex items-center justify-between pt-3 border-t">
                      <div className="text-sm text-gray-600">
                        Reported by:{" "}
                        {student
                          ? `${student.firstName} ${student.lastName} (${student.studentId})`
                          : "Unknown student"}
                        {student?.roomNumber
                          ? ` · Room ${student.roomNumber}`
                          : ""}
                      </div>
                      <div className="flex items-center space-x-3">
                        <span className="text-sm text-gray-600">Status:</span>
                        <select
                          className="input text-sm w-40"
                          value={status}
                          onChange={(e) =>
                            handleStatusChange(complaint.id, e.target.value)
                          }
                        >
                          <option value="PENDING">Pending</option>
                          <option value="IN_PROGRESS">In Progress</option>
                          <option value="RESOLVED">Resolved</option>
                          <option value="CLOSED">Closed</option>
                          <option value="REJECTED">Rejected</option>
                        </select>
                        {user.role === "ADMIN" && (
                          <button
                            onClick={() => handleDeleteComplaint(complaint.id)}
                            className="text-red-600 hover:text-red-800"
                            title="Delete complaint"
                          >
                            <Trash2 className="h-4 w-4" />
                          </button>
                        )}
                      </div>
                    </div>
                  )}
                </div>
              );
            })}
          </div>

          {filteredComplaints.length === 0 && (
            <div className="text-center py-8">
              <MessageSquare className="mx-auto h-12 w-12 text-gray-400" />
              <h3 className="mt-2 text-sm font-medium text-gray-900">
                No complaints
              </h3>
              <p className="mt-1 text-sm text-gray-500">
                {user.role === "STUDENT"
                  ? "You haven't submitted any complaints yet."
                  : "No complaints match your search."}
              </p>
            </div>
          )}
        </div>
      </div>

      {/* Complaint Form Modal */}
      {showComplaintForm && (
        <div className="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
          <div className="relative top-20 mx-auto p-5 border w-11/12 md:w-3/4 lg:w-1/2 shadow-lg rounded-md bg-white">
            <div className="mt-3">
              <h3 className="text-lg font-medium text-gray-900 mb-4">
                {editingComplaint ? "Edit Complaint" : "Submit New Complaint"}
              </h3>
              <form onSubmit={handleFormSubmit} className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700">
                    Title
                  </label>
                  <input
                    type="text"
                    name="title"
                    value={formData.title}
                    onChange={handleInputChange}
                    className="input mt-1"
                    maxLength={100}
                    required
                    placeholder="Title of the Complaint"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700">
                    Description
                  </label>
                  <textarea
                    name="description"
                    value={formData.description}
                    onChange={handleInputChange}
                    className="input mt-1"
                    rows="4"
                    maxLength={1000}
                    required
                    placeholder="Detailed description of the complaint"
                  />
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700">
                      Category
                    </label>
                    <select
                      name="category"
                      value={formData.category}
                      onChange={handleInputChange}
                      className="input mt-1"
                      required
                    >
                      <option value="">Select Category</option>
                      <option value="plumbing">Plumbing</option>
                      <option value="electrical">Electrical</option>
                      <option value="cleaning">Cleaning</option>
                      <option value="maintenance">Maintenance</option>
                      <option value="security">Security</option>
                      <option value="food">Food</option>
                      <option value="internet">Internet</option>
                      <option value="other">Other</option>
                    </select>
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700">
                      Priority
                    </label>
                    <select
                      name="priority"
                      value={formData.priority}
                      onChange={handleInputChange}
                      className="input mt-1"
                    >
                      <option value="low">Low</option>
                      <option value="medium">Medium</option>
                      <option value="high">High</option>
                      <option value="urgent">Urgent</option>
                    </select>
                  </div>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700">
                    Location
                  </label>
                  <input
                    type="text"
                    name="location"
                    value={formData.location}
                    onChange={handleInputChange}
                    className="input mt-1"
                    placeholder="Specific location of the issue"
                  />
                </div>

                <div className="flex justify-end space-x-3 pt-4">
                  <button
                    type="button"
                    onClick={() => setShowComplaintForm(false)}
                    className="btn-outline"
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    className="btn-primary"
                    disabled={submitting}
                  >
                    {submitting
                      ? "Saving..."
                      : editingComplaint
                        ? "Save Changes"
                        : "Submit Complaint"}
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

export default Complaints;
