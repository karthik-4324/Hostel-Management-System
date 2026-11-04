import { useState, useEffect } from 'react'
import { feesAPI } from '../services/api'
import { Plus, Search, CreditCard, DollarSign, AlertCircle } from 'lucide-react'
import { useAuth } from '../contexts/AuthContext'
import toast from 'react-hot-toast'

const Fees = () => {
  const { user } = useAuth()
  const [fees, setFees] = useState([])
  const [loading, setLoading] = useState(true)
  const [searchTerm, setSearchTerm] = useState('')
  const [filterStatus, setFilterStatus] = useState('')
  const [filterType, setFilterType] = useState('')

  useEffect(() => {
    fetchFees()
  }, [searchTerm, filterStatus, filterType])

  const fetchFees = async () => {
    try {
      const params = {
        search: searchTerm,
        status: filterStatus,
        feeType: filterType
      }
      
      const response = await feesAPI.getAll(params)
      if (response.data.success) {
        setFees(response.data.data.fees)
      }
    } catch (error) {
      toast.error('Failed to fetch fees')
    } finally {
      setLoading(false)
    }
  }

  const getStatusColor = (status) => {
    switch (status) {
      case 'paid':
        return 'bg-green-100 text-green-800'
      case 'pending':
        return 'bg-yellow-100 text-yellow-800'
      case 'overdue':
        return 'bg-red-100 text-red-800'
      case 'partial':
        return 'bg-blue-100 text-blue-800'
      default:
        return 'bg-gray-100 text-gray-800'
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
          <h1 className="text-2xl font-bold text-gray-900">Fees</h1>
          <p className="text-gray-600">
            {user.role === 'STUDENT' ? 'View and pay your fees' : 'Manage student fees'}
          </p>
        </div>
        {user.role !== 'STUDENT' && (
          <button className="btn-primary">
            <Plus className="h-4 w-4 mr-2" />
            Add Fee
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
                placeholder="Search fees..."
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
              <option value="paid">Paid</option>
              <option value="overdue">Overdue</option>
              <option value="partial">Partial</option>
            </select>
            <select
              className="input w-full md:w-48"
              value={filterType}
              onChange={(e) => setFilterType(e.target.value)}
            >
              <option value="">All Types</option>
              <option value="room_rent">Room Rent</option>
              <option value="mess_fee">Mess Fee</option>
              <option value="security_deposit">Security Deposit</option>
              <option value="maintenance">Maintenance</option>
              <option value="electricity">Electricity</option>
              <option value="water">Water</option>
              <option value="internet">Internet</option>
              <option value="other">Other</option>
            </select>
          </div>

          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Fee Details
                  </th>
                  {user.role !== 'STUDENT' && (
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Student
                    </th>
                  )}
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Amount
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Due Date
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Status
                  </th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {fees.map((fee) => (
                  <tr key={fee.id}>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="flex items-center">
                        <CreditCard className="h-5 w-5 text-gray-400 mr-3" />
                        <div>
                          <div className="text-sm font-medium text-gray-900">
                            {fee.feeType.replace('_', ' ').toUpperCase()}
                          </div>
                          <div className="text-sm text-gray-500">
                            {fee.month}/{fee.year}
                          </div>
                        </div>
                      </div>
                    </td>
                    {user.role !== 'STUDENT' && (
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="text-sm text-gray-900">
                          {fee.student?.firstName} {fee.student?.lastName}
                        </div>
                        <div className="text-sm text-gray-500">
                          {fee.student?.studentId}
                        </div>
                      </td>
                    )}
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm text-gray-900">₹{fee.finalAmount}</div>
                      {fee.paidAmount > 0 && (
                        <div className="text-sm text-gray-500">
                          Paid: ₹{fee.paidAmount}
                        </div>
                      )}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      {new Date(fee.dueDate).toLocaleDateString()}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getStatusColor(fee.status)}`}>
                        {fee.status}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                      <div className="flex items-center justify-end space-x-2">
                        <button className="text-primary-600 hover:text-primary-900">
                          View Details
                        </button>
                        {fee.status !== 'paid' && (
                          <button className="btn-primary text-xs">
                            {user.role === 'STUDENT' ? 'Pay Now' : 'Add Payment'}
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {fees.length === 0 && (
            <div className="text-center py-8">
              <CreditCard className="mx-auto h-12 w-12 text-gray-400" />
              <h3 className="mt-2 text-sm font-medium text-gray-900">No fees</h3>
              <p className="mt-1 text-sm text-gray-500">
                {user.role === 'STUDENT' 
                  ? "You don't have any fees at the moment." 
                  : "No fees have been created yet."
                }
              </p>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

export default Fees