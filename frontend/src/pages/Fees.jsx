import { useState, useEffect } from "react";
import { feesAPI, studentsAPI, wardenAPI } from "../services/api";
import { Search, CreditCard, Trash2 } from "lucide-react";
import { useAuth } from "../contexts/AuthContext";
import toast from "react-hot-toast";

const emptyPaymentForm = {
  amount: "",
  paymentMethod: "cash",
  transactionId: "",
};

const Fees = () => {
  const { user } = useAuth();
  const isAdmin = user.role === "ADMIN";
  const [fees, setFees] = useState([]);
  const [students, setStudents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState("");
  const [filterStatus, setFilterStatus] = useState("");
  const [showPaymentForm, setShowPaymentForm] = useState(false);
  const [selectedFee, setSelectedFee] = useState(null);
  const [paymentForm, setPaymentForm] = useState(emptyPaymentForm);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    fetchFees();
  }, [filterStatus]);

  useEffect(() => {
    if (user.role !== "STUDENT") {
      fetchStudents();
    }
  }, []);

  const fetchFees = async () => {
    try {
      const response = await feesAPI.getAll({
        status: filterStatus,
        limit: 100,
      });
      if (response.data.success) {
        setFees(response.data.data.fees);
      }
    } catch (error) {
      toast.error("Failed to fetch fees");
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

  const filteredFees = fees.filter((fee) => {
    if (!searchTerm.trim()) return true;
    const s = findStudent(fee.studentId);
    const text =
      `${s?.firstName ?? ""} ${s?.lastName ?? ""} ${s?.studentId ?? ""} ${s?.roomNumber ?? ""}`.toLowerCase();
    return text.includes(searchTerm.toLowerCase());
  });

  const handleOpenPayment = (fee) => {
    setSelectedFee(fee);
    setPaymentForm(emptyPaymentForm);
    setShowPaymentForm(true);
  };

  const handleAddPayment = async (e) => {
    e.preventDefault();
    if (submitting) return;
    setSubmitting(true);
    try {
      await feesAPI.addPayment(selectedFee.id, {
        amount: parseFloat(paymentForm.amount),
        paymentMethod: paymentForm.paymentMethod,
        transactionId: paymentForm.transactionId,
      });
      toast.success("Payment recorded");
      setShowPaymentForm(false);
      fetchFees();
    } catch (error) {
      toast.error(error.response?.data?.message || "Failed to add payment");
    } finally {
      setSubmitting(false);
    }
  };

  const handleDeleteFee = async (id) => {
    if (window.confirm("Delete this fee?")) {
      try {
        await feesAPI.delete(id);
        toast.success("Fee deleted");
        fetchFees();
      } catch (error) {
        toast.error(error.response?.data?.message || "Failed to delete fee");
      }
    }
  };

  const handlePaymentInputChange = (e) => {
    const { name, value } = e.target;
    setPaymentForm({ ...paymentForm, [name]: value });
  };

  const getStatusColor = (status) => {
    switch (status?.toLowerCase()) {
      case "paid":
        return "bg-green-100 text-green-800";
      case "pending":
        return "bg-yellow-100 text-yellow-800";
      case "overdue":
        return "bg-red-100 text-red-800";
      case "partial":
        return "bg-blue-100 text-blue-800";
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
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Fees</h1>
        <p className="text-gray-600">
          {user.role === "STUDENT"
            ? "Your room fee and payments"
            : "Room fees and payments of all students"}
        </p>
      </div>

      <div className="card">
        <div className="card-content">
          <div className="flex flex-col md:flex-row md:items-center space-y-4 md:space-y-0 md:space-x-4 mb-6">
            {user.role !== "STUDENT" && (
              <div className="relative flex-1">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-4 w-4" />
                <input
                  type="text"
                  placeholder="Search by student name, ID or room..."
                  className="input pl-10"
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                />
              </div>
            )}
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
          </div>

          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  {user.role !== "STUDENT" && (
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Student
                    </th>
                  )}
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Fee
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Amount
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Payments
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Status
                  </th>
                  {isAdmin && (
                    <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Actions
                    </th>
                  )}
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {filteredFees.map((fee) => {
                  const student = findStudent(fee.studentId);
                  return (
                    <tr key={fee.id}>
                      {user.role !== "STUDENT" && (
                        <td className="px-6 py-4 whitespace-nowrap">
                          <div className="text-sm font-medium text-gray-900">
                            {student
                              ? `${student.firstName} ${student.lastName}`
                              : "Unknown"}
                          </div>
                          <div className="text-sm text-gray-500">
                            {student?.studentId}
                            {student?.roomNumber
                              ? ` · Room ${student.roomNumber}`
                              : ""}
                          </div>
                          <div className="text-sm text-gray-500">
                            {student?.phone}
                          </div>
                        </td>
                      )}
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="flex items-center">
                          <CreditCard className="h-5 w-5 text-gray-400 mr-3" />
                          <div>
                            <div className="text-sm font-medium text-gray-900">
                              {fee.feeType?.replace("_", " ").toUpperCase()}
                            </div>
                            <div className="text-sm text-gray-500">
                              {fee.month}/{fee.year} · Due{" "}
                              {new Date(fee.dueDate).toLocaleDateString()}
                            </div>
                          </div>
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="text-sm text-gray-900">
                          Total: ₹{fee.finalAmount}
                        </div>
                        <div className="text-sm text-gray-500">
                          Paid: ₹{fee.paidAmount}
                        </div>
                        <div className="text-sm text-gray-500">
                          Balance: ₹{fee.balanceAmount}
                        </div>
                      </td>
                      <td className="px-6 py-4">
                        {fee.paymentHistory?.length > 0 ? (
                          <div className="space-y-1">
                            {fee.paymentHistory.map((p, i) => (
                              <div key={i} className="text-xs text-gray-600">
                                ₹{p.amount} on{" "}
                                {new Date(p.paidDate).toLocaleDateString()} (
                                {p.paymentMethod?.replace("_", " ")})
                              </div>
                            ))}
                          </div>
                        ) : (
                          <span className="text-xs text-gray-400">
                            No payments yet
                          </span>
                        )}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span
                          className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getStatusColor(fee.status)}`}
                        >
                          {fee.status}
                        </span>
                      </td>
                      {isAdmin && (
                        <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                          <div className="flex items-center justify-end space-x-3">
                            {fee.status !== "PAID" && (
                              <button
                                onClick={() => handleOpenPayment(fee)}
                                className="btn-primary text-xs"
                              >
                                Add Payment
                              </button>
                            )}
                            <button
                              onClick={() => handleDeleteFee(fee.id)}
                              className="text-red-600 hover:text-red-800"
                              title="Delete fee"
                            >
                              <Trash2 className="h-4 w-4" />
                            </button>
                          </div>
                        </td>
                      )}
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>

          {filteredFees.length === 0 && (
            <div className="text-center py-8">
              <CreditCard className="mx-auto h-12 w-12 text-gray-400" />
              <h3 className="mt-2 text-sm font-medium text-gray-900">
                No fees
              </h3>
              <p className="mt-1 text-sm text-gray-500">
                {user.role === "STUDENT"
                  ? "You don't have any fees at the moment."
                  : "Fees appear here when a student is allocated a room."}
              </p>
            </div>
          )}
        </div>
      </div>

      {/* Add Payment Modal */}
      {showPaymentForm && selectedFee && (
        <div className="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
          <div className="relative top-20 mx-auto p-5 border w-96 shadow-lg rounded-md bg-white">
            <h3 className="text-lg font-medium text-gray-900 mb-1">
              Record Payment
            </h3>
            <p className="text-sm text-gray-500 mb-4">
              Balance due: ₹{selectedFee.balanceAmount}
            </p>
            <form onSubmit={handleAddPayment} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700">
                  Amount (₹)
                </label>
                <input
                  type="number"
                  name="amount"
                  value={paymentForm.amount}
                  onChange={handlePaymentInputChange}
                  className="input mt-1"
                  min="1"
                  max={selectedFee.balanceAmount}
                  step="0.01"
                  required
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700">
                  Payment Method
                </label>
                <select
                  name="paymentMethod"
                  value={paymentForm.paymentMethod}
                  onChange={handlePaymentInputChange}
                  className="input mt-1"
                >
                  <option value="cash">Cash</option>
                  <option value="upi">UPI</option>
                  <option value="card">Card</option>
                  <option value="bank_transfer">Bank Transfer</option>
                  <option value="cheque">Cheque</option>
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700">
                  Transaction ID (optional)
                </label>
                <input
                  type="text"
                  name="transactionId"
                  value={paymentForm.transactionId}
                  onChange={handlePaymentInputChange}
                  className="input mt-1"
                />
              </div>
              <div className="flex justify-end space-x-3">
                <button
                  type="button"
                  onClick={() => setShowPaymentForm(false)}
                  className="btn-outline"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="btn-primary"
                  disabled={submitting}
                >
                  {submitting ? "Saving..." : "Record Payment"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default Fees;
