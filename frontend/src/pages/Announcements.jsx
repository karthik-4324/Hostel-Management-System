import { useState, useEffect } from "react";
import { announcementsAPI } from "../services/api";
import {
  Plus,
  Search,
  Megaphone,
  Heart,
  Calendar,
  Edit,
  Trash2,
} from "lucide-react";
import { useAuth } from "../contexts/AuthContext";
import toast from "react-hot-toast";

const emptyForm = {
  title: "",
  content: "",
  type: "general",
  priority: "medium",
  targetAudience: "all",
  isSticky: false,
  expiryDate: "",
};

const todayStr = () => {
  const d = new Date();
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;
};

const Announcements = () => {
  const { user } = useAuth();
  const isAdmin = user.role === "ADMIN";
  const [announcements, setAnnouncements] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState("");
  const [filterType, setFilterType] = useState("");
  const [filterPriority, setFilterPriority] = useState("");
  const [showForm, setShowForm] = useState(false);
  const [editingAnnouncement, setEditingAnnouncement] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const [formData, setFormData] = useState(emptyForm);

  useEffect(() => {
    fetchAnnouncements();
  }, [searchTerm, filterType, filterPriority]);

  const fetchAnnouncements = async () => {
    try {
      const params = {
        search: searchTerm,
        type: filterType,
        priority: filterPriority,
        limit: 100,
      };

      const response = await announcementsAPI.getAll(params);
      if (response.data.success) {
        const list = response.data.data.announcements;
        // pinned announcements first
        list.sort(
          (a, b) => Number(b.sticky ?? false) - Number(a.sticky ?? false),
        );
        setAnnouncements(list);
      }
    } catch (error) {
      toast.error("Failed to fetch announcements");
    } finally {
      setLoading(false);
    }
  };

  const handleCreateAnnouncement = () => {
    setEditingAnnouncement(null);
    setFormData(emptyForm);
    setShowForm(true);
  };

  const handleEditAnnouncement = (announcement) => {
    setEditingAnnouncement(announcement);
    setFormData({
      title: announcement.title || "",
      content: announcement.content || "",
      type: (announcement.type || "general").toLowerCase(),
      priority: (announcement.priority || "medium").toLowerCase(),
      targetAudience: (announcement.targetAudience || "all").toLowerCase(),
      isSticky: announcement.sticky ?? false,
      expiryDate: announcement.expiryDate
        ? announcement.expiryDate.slice(0, 10)
        : "",
    });
    setShowForm(true);
  };

  const handleFormSubmit = async (e) => {
    e.preventDefault();
    if (submitting) return;
    setSubmitting(true);
    try {
      const payload = {
        title: formData.title,
        content: formData.content,
        type: formData.type.toUpperCase(),
        priority: formData.priority.toUpperCase(),
        targetAudience: formData.targetAudience.toUpperCase(),
        sticky: formData.isSticky,
        expiryDate: formData.expiryDate
          ? `${formData.expiryDate}T23:59:59`
          : null,
      };

      if (editingAnnouncement) {
        await announcementsAPI.update(editingAnnouncement.id, payload);
        toast.success("Announcement updated successfully");
      } else {
        await announcementsAPI.create(payload);
        toast.success("Announcement created successfully");
      }
      setShowForm(false);
      fetchAnnouncements();
    } catch (error) {
      toast.error(
        error.response?.data?.message ||
          (editingAnnouncement
            ? "Failed to update announcement"
            : "Failed to create announcement"),
      );
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm("Delete this announcement?")) return;
    try {
      await announcementsAPI.delete(id);
      toast.success("Announcement deleted");
      fetchAnnouncements();
    } catch (error) {
      toast.error(
        error.response?.data?.message || "Failed to delete announcement",
      );
    }
  };

  const handleLike = async (id) => {
    try {
      await announcementsAPI.toggleLike(id);
      fetchAnnouncements();
    } catch (error) {
      toast.error("Failed to update like");
    }
  };

  const handleInputChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData({
      ...formData,
      [name]: type === "checkbox" ? checked : value,
    });
  };

  const getPriorityColor = (priority) => {
    switch (priority?.toLowerCase()) {
      case "critical":
        return "bg-red-100 text-red-800 border-red-200";
      case "high":
        return "bg-orange-100 text-orange-800 border-orange-200";
      case "medium":
        return "bg-yellow-100 text-yellow-800 border-yellow-200";
      case "low":
        return "bg-green-100 text-green-800 border-green-200";
      default:
        return "bg-gray-100 text-gray-800 border-gray-200";
    }
  };

  const isExpired = (a) => a.expiryDate && new Date(a.expiryDate) < new Date();

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
            Announcements
          </h1>
          <p className="text-gray-600">
            {user.role === "STUDENT"
              ? "Stay updated with the latest announcements"
              : "Manage and publish announcements"}
          </p>
        </div>
        {isAdmin && (
          <button onClick={handleCreateAnnouncement} className="btn-primary">
            <Plus className="h-4 w-4 mr-2" />
            Create Announcement
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
                placeholder="Search announcements..."
                className="input pl-10"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
              />
            </div>
            <select
              className="input w-full md:w-48"
              value={filterType}
              onChange={(e) => setFilterType(e.target.value)}
            >
              <option value="">All Types</option>
              <option value="general">General</option>
              <option value="urgent">Urgent</option>
              <option value="event">Event</option>
              <option value="maintenance">Maintenance</option>
              <option value="fee">Fee</option>
              <option value="academic">Academic</option>
              <option value="holiday">Holiday</option>
            </select>
            <select
              className="input w-full md:w-48"
              value={filterPriority}
              onChange={(e) => setFilterPriority(e.target.value)}
            >
              <option value="">All Priorities</option>
              <option value="low">Low</option>
              <option value="medium">Medium</option>
              <option value="high">High</option>
              <option value="critical">Critical</option>
            </select>
          </div>

          <div className="space-y-6">
            {announcements.map((announcement) => (
              <div
                key={announcement.id}
                className={`border rounded-lg p-6 ${announcement.sticky ? "border-primary-200 bg-primary-50" : "hover:shadow-md"} transition-shadow`}
              >
                <div className="flex justify-between items-start mb-4">
                  <div className="flex-1">
                    <div className="flex flex-wrap items-center gap-3 mb-2">
                      {announcement.sticky && (
                        <div className="flex items-center text-primary-600">
                          <Megaphone className="h-4 w-4 mr-1" />
                          <span className="text-xs font-medium">PINNED</span>
                        </div>
                      )}
                      <span
                        className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getPriorityColor(announcement.priority)}`}
                      >
                        {announcement.priority}
                      </span>
                      <span className="text-xs text-gray-500">
                        {announcement.type?.toUpperCase()}
                      </span>
                      {isAdmin && (
                        <span className="text-xs text-gray-500">
                          For: {announcement.targetAudience}
                        </span>
                      )}
                      {isAdmin && isExpired(announcement) && (
                        <span className="inline-flex px-2 py-1 text-xs font-semibold rounded-full bg-gray-200 text-gray-700">
                          EXPIRED
                        </span>
                      )}
                    </div>
                    <h3 className="text-xl font-semibold text-gray-900 mb-2">
                      {announcement.title}
                    </h3>
                    <p className="text-gray-600 mb-4 leading-relaxed">
                      {announcement.content}
                    </p>
                    <div className="flex flex-wrap items-center gap-x-4 gap-y-1 text-sm text-gray-500">
                      <div className="flex items-center">
                        <Calendar className="h-4 w-4 mr-1" />
                        {new Date(
                          announcement.publishDate,
                        ).toLocaleDateString()}
                      </div>
                      <div>By: {announcement.authorName || "Admin"}</div>
                      <div>Views: {announcement.viewCount || 0}</div>
                      {announcement.expiryDate && (
                        <div>
                          Expires:{" "}
                          {new Date(
                            announcement.expiryDate,
                          ).toLocaleDateString()}
                        </div>
                      )}
                    </div>
                  </div>
                </div>

                <div className="flex items-center justify-between pt-4 border-t">
                  <button
                    onClick={() => handleLike(announcement.id)}
                    className="flex items-center space-x-1 text-gray-500 hover:text-red-500 transition-colors"
                  >
                    <Heart className="h-4 w-4" />
                    <span className="text-sm">
                      {announcement.likeCount || 0}
                    </span>
                  </button>

                  {isAdmin && (
                    <div className="flex items-center space-x-4">
                      <button
                        onClick={() => handleEditAnnouncement(announcement)}
                        className="flex items-center text-sm text-blue-600 hover:text-blue-800"
                      >
                        <Edit className="h-4 w-4 mr-1" />
                        Edit
                      </button>
                      <button
                        onClick={() => handleDelete(announcement.id)}
                        className="flex items-center text-sm text-red-600 hover:text-red-800"
                      >
                        <Trash2 className="h-4 w-4 mr-1" />
                        Delete
                      </button>
                    </div>
                  )}
                </div>
              </div>
            ))}
          </div>

          {announcements.length === 0 && (
            <div className="text-center py-8">
              <Megaphone className="mx-auto h-12 w-12 text-gray-400" />
              <h3 className="mt-2 text-sm font-medium text-gray-900">
                No announcements
              </h3>
              <p className="mt-1 text-sm text-gray-500">
                {isAdmin
                  ? "Create your first announcement to get started."
                  : "No announcements have been posted yet."}
              </p>
            </div>
          )}
        </div>
      </div>

      {/* Announcement Form Modal */}
      {showForm && (
        <div className="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
          <div className="relative top-20 mx-auto p-5 border w-11/12 md:w-3/4 lg:w-1/2 shadow-lg rounded-md bg-white">
            <div className="mt-3">
              <h3 className="text-lg font-medium text-gray-900 mb-4">
                {editingAnnouncement
                  ? "Edit Announcement"
                  : "Create New Announcement"}
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
                    maxLength={200}
                    required
                    placeholder="Announcement title"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700">
                    Content
                  </label>
                  <textarea
                    name="content"
                    value={formData.content}
                    onChange={handleInputChange}
                    className="input mt-1"
                    rows="4"
                    maxLength={2000}
                    required
                    placeholder="Announcement content"
                  />
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700">
                      Type
                    </label>
                    <select
                      name="type"
                      value={formData.type}
                      onChange={handleInputChange}
                      className="input mt-1"
                    >
                      <option value="general">General</option>
                      <option value="urgent">Urgent</option>
                      <option value="event">Event</option>
                      <option value="maintenance">Maintenance</option>
                      <option value="fee">Fee</option>
                      <option value="academic">Academic</option>
                      <option value="holiday">Holiday</option>
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
                      <option value="critical">Critical</option>
                    </select>
                  </div>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700">
                    Target Audience
                  </label>
                  <select
                    name="targetAudience"
                    value={formData.targetAudience}
                    onChange={handleInputChange}
                    className="input mt-1"
                  >
                    <option value="all">All Users</option>
                    <option value="students">Students Only</option>
                    <option value="wardens">Wardens Only</option>
                    <option value="admins">Admins Only</option>
                  </select>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700">
                    Expiry Date (Optional)
                  </label>
                  <input
                    type="date"
                    name="expiryDate"
                    value={formData.expiryDate}
                    onChange={handleInputChange}
                    min={todayStr()}
                    className="input mt-1"
                  />
                </div>

                <div className="flex items-center">
                  <input
                    type="checkbox"
                    name="isSticky"
                    checked={formData.isSticky}
                    onChange={handleInputChange}
                    className="mr-2"
                  />
                  <label className="text-sm font-medium text-gray-700">
                    Pin this announcement
                  </label>
                </div>

                <div className="flex justify-end space-x-3 pt-4">
                  <button
                    type="button"
                    onClick={() => setShowForm(false)}
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
                      : editingAnnouncement
                        ? "Save Changes"
                        : "Create Announcement"}
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

export default Announcements;
