import { useState, useEffect } from 'react'
import { useAuth } from '../contexts/AuthContext'
import { User, Mail, Phone, Calendar, MapPin } from 'lucide-react'

const Profile = () => {
  const { user, updateProfile } = useAuth()
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    phone: '',
    emergencyContact: {
      name: '',
      phone: '',
      relation: ''
    },
    course: '',
    year: '',
    department: ''
  })
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    if (user) {
      setFormData({
        firstName: user.firstName || '',
        lastName: user.lastName || '',
        phone: user.phone || '',
        emergencyContact: user.emergencyContact || {
          name: '',
          phone: '',
          relation: ''
        },
        course: user.course || '',
        year: user.year || '',
        department: user.department || ''
      })
    }
  }, [user])

  const handleChange = (e) => {
    const { name, value } = e.target
    
    if (name.startsWith('emergencyContact.')) {
      const field = name.split('.')[1]
      setFormData({
        ...formData,
        emergencyContact: {
          ...formData.emergencyContact,
          [field]: value
        }
      })
    } else {
      setFormData({
        ...formData,
        [name]: value
      })
    }
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    
    await updateProfile(formData)
    setLoading(false)
  }

  return (
    <div className="max-w-4xl mx-auto">
      <div className="space-y-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Profile</h1>
          <p className="text-gray-600">Manage your account information</p>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Profile Info Card */}
          <div className="card">
            <div className="card-content">
              <div className="text-center">
                <div className="w-20 h-20 bg-primary-600 rounded-full flex items-center justify-center mx-auto mb-4">
                  <User className="h-10 w-10 text-white" />
                </div>
                <h3 className="text-lg font-medium text-gray-900">
                  {user?.firstName} {user?.lastName}
                </h3>
                <p className="text-sm text-gray-500">{user?.role}</p>
                
                <div className="mt-4 space-y-2">
                  <div className="flex items-center justify-center text-sm text-gray-600">
                    <Mail className="h-4 w-4 mr-2" />
                    {user?.email}
                  </div>
                  <div className="flex items-center justify-center text-sm text-gray-600">
                    <Phone className="h-4 w-4 mr-2" />
                    {user?.phone}
                  </div>
                  {user?.studentId && (
                    <div className="flex items-center justify-center text-sm text-gray-600">
                      <User className="h-4 w-4 mr-2" />
                      ID: {user.studentId}
                    </div>
                  )}
                  {user?.employeeId && (
                    <div className="flex items-center justify-center text-sm text-gray-600">
                      <User className="h-4 w-4 mr-2" />
                      ID: {user.employeeId}
                    </div>
                  )}
                </div>
              </div>
            </div>
          </div>

          {/* Edit Profile Form */}
          <div className="lg:col-span-2">
            <div className="card">
              <div className="card-header">
                <h3 className="card-title">Edit Profile</h3>
              </div>
              <div className="card-content">
                <form onSubmit={handleSubmit} className="space-y-6">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                      <label htmlFor="firstName" className="block text-sm font-medium text-gray-700">
                        First Name
                      </label>
                      <input
                        id="firstName"
                        name="firstName"
                        type="text"
                        className="input mt-1"
                        value={formData.firstName}
                        onChange={handleChange}
                      />
                    </div>
                    
                    <div>
                      <label htmlFor="lastName" className="block text-sm font-medium text-gray-700">
                        Last Name
                      </label>
                      <input
                        id="lastName"
                        name="lastName"
                        type="text"
                        className="input mt-1"
                        value={formData.lastName}
                        onChange={handleChange}
                      />
                    </div>
                    
                    <div>
                      <label htmlFor="phone" className="block text-sm font-medium text-gray-700">
                        Phone
                      </label>
                      <input
                        id="phone"
                        name="phone"
                        type="tel"
                        className="input mt-1"
                        value={formData.phone}
                        onChange={handleChange}
                      />
                    </div>
                    
                    {user?.role === 'STUDENT' && (
                      <>
                        <div>
                          <label htmlFor="course" className="block text-sm font-medium text-gray-700">
                            Course
                          </label>
                          <input
                            id="course"
                            name="course"
                            type="text"
                            className="input mt-1"
                            value={formData.course}
                            onChange={handleChange}
                          />
                        </div>
                        
                        <div>
                          <label htmlFor="year" className="block text-sm font-medium text-gray-700">
                            Year
                          </label>
                          <select
                            id="year"
                            name="year"
                            className="input mt-1"
                            value={formData.year}
                            onChange={handleChange}
                          >
                            <option value="">Select Year</option>
                            <option value="1">1st Year</option>
                            <option value="2">2nd Year</option>
                            <option value="3">3rd Year</option>
                            <option value="4">4th Year</option>
                          </select>
                        </div>
                      </>
                    )}
                    
                    {(user?.role === 'WARDEN' || user?.role === 'ADMIN') && (
                      <div>
                        <label htmlFor="department" className="block text-sm font-medium text-gray-700">
                          Department
                        </label>
                        <input
                          id="department"
                          name="department"
                          type="text"
                          className="input mt-1"
                          value={formData.department}
                          onChange={handleChange}
                        />
                      </div>
                    )}
                  </div>
                  
                  <div className="border-t pt-6">
                    <h4 className="text-lg font-medium text-gray-900 mb-4">Emergency Contact</h4>
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                      <div>
                        <label htmlFor="emergencyContact.name" className="block text-sm font-medium text-gray-700">
                          Name
                        </label>
                        <input
                          id="emergencyContact.name"
                          name="emergencyContact.name"
                          type="text"
                          className="input mt-1"
                          value={formData.emergencyContact.name}
                          onChange={handleChange}
                        />
                      </div>
                      
                      <div>
                        <label htmlFor="emergencyContact.phone" className="block text-sm font-medium text-gray-700">
                          Phone
                        </label>
                        <input
                          id="emergencyContact.phone"
                          name="emergencyContact.phone"
                          type="tel"
                          className="input mt-1"
                          value={formData.emergencyContact.phone}
                          onChange={handleChange}
                        />
                      </div>
                      
                      <div>
                        <label htmlFor="emergencyContact.relation" className="block text-sm font-medium text-gray-700">
                          Relation
                        </label>
                        <input
                          id="emergencyContact.relation"
                          name="emergencyContact.relation"
                          type="text"
                          className="input mt-1"
                          value={formData.emergencyContact.relation}
                          onChange={handleChange}
                        />
                      </div>
                    </div>
                  </div>

                  <div className="flex justify-end">
                    <button
                      type="submit"
                      disabled={loading}
                      className="btn-primary"
                    >
                      {loading ? 'Updating...' : 'Update Profile'}
                    </button>
                  </div>
                </form>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}

export default Profile