import { useState, useEffect } from 'react'
import { announcementsAPI } from '../services/api'
import { Plus, Search, Megaphone, Heart, MessageCircle, Calendar } from 'lucide-react'
import { useAuth } from '../contexts/AuthContext'
import toast from 'react-hot-toast'

const Announcements = () => {
  const { user } = useAuth()
  const [announcements, setAnnouncements] = useState([])
  const [loading, setLoading] = useState(true)
  const [searchTerm, setSearchTerm] = useState('')
  const [filterType, setFilterType] = useState('')
  const [filterPriority, setFilterPriority] = useState('')
  const [showAnnouncementForm, setShowAnnouncementForm] = useState(false)
  const [formData, setFormData] = useState({
    title: '',
    content: '',
    type: 'general',
    priority: 'medium',
    targetAudience: 'all',
    isSticky: false,
    expiryDate: ''
  })

  useEffect(() => {
    fetchAnnouncements()
  }, [searchTerm, filterType, filterPriority])

  const fetchAnnouncements = async () => {
    try {
      const params = {
        search: searchTerm,
        type: filterType,
        priority: filterPriority
      }
      
      const response = await announcementsAPI.getAll(params)
      if (response.data.success) {
        setAnnouncements(response.data.data.announcements)
      }
    } catch (error) {
      toast.error('Failed to fetch announcements')
    } finally {
      setLoading(false)
    }
  }

  const handleCreateAnnouncement = () => {
    setFormData({
      title: '',
      content: '',
      type: 'general',
      priority: 'medium',
      targetAudience: 'all',
      isSticky: false,
      expiryDate: ''
    })
    setShowAnnouncementForm(true)
  }

  const handleFormSubmit = async (e) => {
    e.preventDefault()
    try {
      const announcementData = {
        ...formData,
        status: 'published'
      }
      
      await announcementsAPI.create(announcementData)
      toast.success('Announcement created successfully')
      setShowAnnouncementForm(false)
      fetchAnnouncements()
    } catch (error) {
      toast.error('Failed to create announcement')
    }
  }

  const handleInputChange = (e) => {
    const { name, value, type, checked } = e.target
    setFormData({
      ...formData,
      [name]: type === 'checkbox' ? checked : value
    })
  }

  const getPriorityColor = (priority) => {
    switch (priority) {
      case 'critical':
        return 'bg-red-100 text-red-800 border-red-200'
      case 'high':
        return 'bg-orange-100 text-orange-800 border-orange-200'
      case 'medium':
        return 'bg-yellow-100 text-yellow-800 border-yellow-200'
      case 'low':
        return 'bg-green-100 text-green-800 border-green-200'
      default:
        return 'bg-gray-100 text-gray-800 border-gray-200'
    }
  }

  const handleLike = async (id) => {
    try {
      await announcementsAPI.toggleLike(id)
      fetchAnnouncements() // Refresh to get updated like count
    } catch (error) {
      toast.error('Failed to update like')
    }
  }

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="loading-spinner"></div>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Announcements</h1>
          <p className="text-gray-600">
            {user.role === 'STUDENT' ? 'Stay updated with latest announcements' : 'Manage announcements'}
          </p>
        </div>
        {(user.role === 'ADMIN' || user.role === 'WARDEN') && (
          <button onClick={handleCreateAnnouncement} className="btn-primary">
            <Plus className="h-4 w-4 mr-2" />
            Create Announcement
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
                className={`border rounded-lg p-6 ${announcement.isSticky ? 'border-primary-200 bg-primary-50' : 'hover:shadow-md'} transition-shadow`}
              >
                <div className="flex justify-between items-start mb-4">
                  <div className="flex-1">
                    <div className="flex items-center space-x-3 mb-2">
                      {announcement.isSticky && (
                        <div className="flex items-center text-primary-600">
                          <Megaphone className="h-4 w-4 mr-1" />
                          <span className="text-xs font-medium">PINNED</span>
                        </div>
                      )}
                      <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getPriorityColor(announcement.priority)}`}>
                        {announcement.priority}
                      </span>
                      <span className="text-xs text-gray-500">
                        {announcement.type.toUpperCase()}
                      </span>
                    </div>
                    <h3 className="text-xl font-semibold text-gray-900 mb-2">
                      {announcement.title}
                    </h3>
                    <p className="text-gray-600 mb-4 leading-relaxed">
                      {announcement.content}
                    </p>
                    <div className="flex items-center space-x-4 text-sm text-gray-500">
                      <div className="flex items-center">
                        <Calendar className="h-4 w-4 mr-1" />
                        {new Date(announcement.publishDate).toLocaleDateString()}
                      </div>
                      <div>
                        By: {announcement.createdBy?.firstName} {announcement.createdBy?.lastName}
                      </div>
                      <div>
                        Views: {announcement.viewCount || 0}
                      </div>
                    </div>
                  </div>
                </div>
                
                <div className="flex items-center justify-between pt-4 border-t">
                  <div className="flex items-center space-x-4">
                    <button 
                      onClick={() => handleLike(announcement.id)}
                      className="flex items-center space-x-1 text-gray-500 hover:text-red-500 transition-colors"
                    >
                      <Heart className="h-4 w-4" />
                      <span className="text-sm">{announcement.likeCount || 0}</span>
                    </button>
                    <div className="flex items-center space-x-1 text-gray-500">
                      <MessageCircle className="h-4 w-4" />
                      <span className="text-sm">{announcement.commentCount || 0}</span>
                    </div>
                  </div>
                  
                  {(user.role === 'ADMIN' || user.role === 'WARDEN') && (
                    <div className="flex space-x-2">
                      <button className="btn-outline text-xs">Edit</button>
                      <button className="text-red-600 hover:text-red-800 text-xs">Delete</button>
                    </div>
                  )}
                </div>
              </div>
            ))}
          </div>

          {announcements.length === 0 && (
            <div className="text-center py-8">
              <Megaphone className="mx-auto h-12 w-12 text-gray-400" />
              <h3 className="mt-2 text-sm font-medium text-gray-900">No announcements</h3>
              <p className="mt-1 text-sm text-gray-500">
                {user.role === 'STUDENT' 
                  ? "No announcements have been posted yet." 
                  : "Create your first announcement to get started."
                }
              </p>
            </div>
          )}
        </div>
      </div>

      {/* Announcement Form Modal */}
      {showAnnouncementForm && (
        <div className="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
          <div className="relative top-20 mx-auto p-5 border w-11/12 md:w-3/4 lg:w-1/2 shadow-lg rounded-md bg-white">
            <div className="mt-3">
              <h3 className="text-lg font-medium text-gray-900 mb-4">
                Create New Announcement
              </h3>
              <form onSubmit={handleFormSubmit} className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700">Title</label>
                  <input
                    type="text"
                    name="title"
                    value={formData.title}
                    onChange={handleInputChange}
                    className="input mt-1"
                    required
                    placeholder="Announcement title"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700">Content</label>
                  <textarea
                    name="content"
                    value={formData.content}
                    onChange={handleInputChange}
                    className="input mt-1"
                    rows="4"
                    required
                    placeholder="Announcement content"
                  />
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700">Type</label>
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
                    <label className="block text-sm font-medium text-gray-700">Priority</label>
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
                  <label className="block text-sm font-medium text-gray-700">Target Audience</label>
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
                  <label className="block text-sm font-medium text-gray-700">Expiry Date (Optional)</label>
                  <input
                    type="date"
                    name="expiryDate"
                    value={formData.expiryDate}
                    onChange={handleInputChange}
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
                  <label className="text-sm font-medium text-gray-700">Pin this announcement</label>
                </div>

                <div className="flex justify-end space-x-3 pt-4">
                  <button
                    type="button"
                    onClick={() => setShowAnnouncementForm(false)}
                    className="btn-outline"
                  >
                    Cancel
                  </button>
                  <button type="submit" className="btn-primary">
                    Create Announcement
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

export default Announcements