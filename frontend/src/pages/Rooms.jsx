import { useState, useEffect } from "react";
import { roomsAPI } from "../services/api";
import { Plus, Search, Building, Users, Edit, Trash2 } from "lucide-react";
import { useAuth } from "../contexts/AuthContext";
import toast from "react-hot-toast";

const Rooms = () => {
  const { user } = useAuth();
  const [rooms, setRooms] = useState([]);
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState("");
  const [filterStatus, setFilterStatus] = useState("");
  const [showRoomForm, setShowRoomForm] = useState(false);
  const [editingRoom, setEditingRoom] = useState(null);
  const [formData, setFormData] = useState({
    roomNumber: "",
    floor: "",
    block: "",
    type: "",
    capacity: "",
    monthlyRent: "",
    securityDeposit: "",
    amenities: [],
    description: "",
  });

  useEffect(() => {
    fetchRooms();
    fetchStats();
  }, [searchTerm, filterStatus]);

  const fetchRooms = async () => {
    try {
      const params = {
        search: searchTerm,
        status: filterStatus,
      };

      const response = await roomsAPI.getAll(params);
      if (response.data.success) {
        setRooms(response.data.data.rooms);
      }
    } catch (error) {
      toast.error("Failed to fetch rooms");
    } finally {
      setLoading(false);
    }
  };

  const fetchStats = async () => {
    try {
      const response = await roomsAPI.getStats();
      if (response.data.success) {
        setStats(response.data.data);
      }
    } catch (error) {
      console.error("Failed to fetch room stats");
    }
  };

  const handleAddRoom = () => {
    setEditingRoom(null);
    setFormData({
      roomNumber: "",
      floor: "",
      block: "",
      type: "",
      capacity: "",
      monthlyRent: "",
      securityDeposit: "",
      amenities: [],
      description: "",
    });
    setShowRoomForm(true);
  };

  const handleEditRoom = (room) => {
    setEditingRoom(room);
    setFormData({
      roomNumber: room.roomNumber,
      floor: room.floor.toString(),
      block: room.block,
      type: room.type,
      capacity: room.capacity.toString(),
      monthlyRent: room.monthlyRent.toString(),
      securityDeposit: room.securityDeposit.toString(),
      amenities: room.amenities || [],
      description: room.description || "",
    });
    setShowRoomForm(true);
  };

  const handleDeleteRoom = async (id) => {
    if (window.confirm("Are you sure you want to delete this room?")) {
      try {
        await roomsAPI.delete(id);
        toast.success("Room deleted successfully");
        fetchRooms();
        fetchStats();
      } catch (error) {
        toast.error(error.response?.data?.message || "Failed to delete room");
      }
    }
  };

  const handleFormSubmit = async (e) => {
    e.preventDefault();
    try {
      const roomData = {
        ...formData,
        floor: parseInt(formData.floor),
        capacity: parseInt(formData.capacity),
        monthlyRent: parseFloat(formData.monthlyRent),
        securityDeposit: parseFloat(formData.securityDeposit),
      };

      if (editingRoom) {
        await roomsAPI.update(editingRoom.id, roomData);
        toast.success("Room updated successfully");
      } else {
        await roomsAPI.create(roomData);
        toast.success("Room created successfully");
      }
      setShowRoomForm(false);
      fetchRooms();
      fetchStats();
    } catch (error) {
      toast.error(
        editingRoom ? "Failed to update room" : "Failed to create room",
      );
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: value,
    });
  };

  const handleAmenityChange = (amenity) => {
    const updatedAmenities = formData.amenities.includes(amenity)
      ? formData.amenities.filter((a) => a !== amenity)
      : [...formData.amenities, amenity];

    setFormData({
      ...formData,
      amenities: updatedAmenities,
    });
  };

  const getStatusColor = (status) => {
    switch (status) {
      case "available":
        return "bg-green-100 text-green-800";
      case "occupied":
        return "bg-blue-100 text-blue-800";
      case "maintenance":
        return "bg-yellow-100 text-yellow-800";
      default:
        return "bg-gray-100 text-gray-800";
    }
  };

  const amenityOptions = [
    "ac",
    "fan",
    "wifi",
    "study_table",
    "wardrobe",
    "attached_bathroom",
    "balcony",
    "tv",
  ];

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
            Rooms
          </h1>
          <p className="text-gray-600">Manage hostel rooms and occupancy</p>
        </div>
        {user.role === "ADMIN" && (
          <button onClick={handleAddRoom} className="btn-primary">
            <Plus className="h-4 w-4 mr-2" />
            Add Room
          </button>
        )}
      </div>

      {/* Stats Cards */}
      {stats && (
        <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
          <div className="card">
            <div className="card-content">
              <div className="flex items-center">
                <Building className="h-8 w-8 text-blue-600" />
                <div className="ml-4">
                  <p className="text-sm font-medium text-gray-500">
                    Total Rooms
                  </p>
                  <p className="text-2xl font-bold text-gray-900">
                    {stats.total}
                  </p>
                </div>
              </div>
            </div>
          </div>

          <div className="card">
            <div className="card-content">
              <div className="flex items-center">
                <Users className="h-8 w-8 text-green-600" />
                <div className="ml-4">
                  <p className="text-sm font-medium text-gray-500">Occupied</p>
                  <p className="text-2xl font-bold text-gray-900">
                    {stats.occupied}
                  </p>
                </div>
              </div>
            </div>
          </div>

          <div className="card">
            <div className="card-content">
              <div className="flex items-center">
                <Building className="h-8 w-8 text-yellow-600" />
                <div className="ml-4">
                  <p className="text-sm font-medium text-gray-500">Available</p>
                  <p className="text-2xl font-bold text-gray-900">
                    {stats.available}
                  </p>
                </div>
              </div>
            </div>
          </div>

          <div className="card">
            <div className="card-content">
              <div className="flex items-center">
                <Building className="h-8 w-8 text-purple-600" />
                <div className="ml-4">
                  <p className="text-sm font-medium text-gray-500">Occupancy</p>
                  <p className="text-2xl font-bold text-gray-900">
                    {stats.occupancyPercentage}%
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

      <div className="card">
        <div className="card-content">
          <div className="flex items-center space-x-4 mb-6">
            <div className="relative flex-1">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-4 w-4" />
              <input
                type="text"
                placeholder="Search rooms..."
                className="input pl-10"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
              />
            </div>
            <select
              className="input w-48"
              value={filterStatus}
              onChange={(e) => setFilterStatus(e.target.value)}
            >
              <option value="">All Status</option>
              <option value="available">Available</option>
              <option value="occupied">Occupied</option>
              <option value="maintenance">Maintenance</option>
            </select>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {rooms.map((room) => (
              <div
                key={room.id}
                className="border rounded-lg p-4 hover:shadow-md transition-shadow"
              >
                <div className="flex justify-between items-start mb-3">
                  <div>
                    <h3 className="text-lg font-semibold text-gray-900">
                      Room {room.roomNumber}
                    </h3>
                    <p className="text-sm text-gray-500">
                      Block {room.block}, Floor {room.floor}
                    </p>
                  </div>
                  <span
                    className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getStatusColor(room.status)}`}
                  >
                    {room.status}
                  </span>
                </div>

                <div className="space-y-2 text-sm">
                  <div className="flex justify-between">
                    <span className="text-gray-500">Type:</span>
                    <span className="font-medium">{room.type}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-500">Capacity:</span>
                    <span className="font-medium">{room.capacity}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-500">Occupancy:</span>
                    <span className="font-medium">
                      {room.currentOccupancy}/{room.capacity}
                    </span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-500">Rent:</span>
                    <span className="font-medium">₹{room.monthlyRent}</span>
                  </div>
                </div>

                {user.role === "ADMIN" && (
                  <div className="mt-4 flex space-x-2">
                    <button
                      onClick={() => handleEditRoom(room)}
                      className="btn-outline text-xs flex-1"
                    >
                      <Edit className="h-3 w-3 mr-1" />
                      Edit
                    </button>
                    {user.role === "ADMIN" && (
                      <button
                        onClick={() => handleDeleteRoom(room.id)}
                        className="text-red-600 hover:text-red-800 text-xs px-2"
                      >
                        <Trash2 className="h-3 w-3" />
                      </button>
                    )}
                  </div>
                )}
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Room Form Modal */}
      {showRoomForm && (
        <div className="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
          <div className="relative top-20 mx-auto p-5 border w-11/12 md:w-3/4 lg:w-1/2 shadow-lg rounded-md bg-white">
            <div className="mt-3">
              <h3 className="text-lg font-medium text-gray-900 mb-4">
                {editingRoom ? "Edit Room" : "Add New Room"}
              </h3>
              <form onSubmit={handleFormSubmit} className="space-y-4">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700">
                      Room Number
                    </label>
                    <input
                      type="text"
                      name="roomNumber"
                      value={formData.roomNumber}
                      onChange={handleInputChange}
                      className="input mt-1"
                      required
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700">
                      Block
                    </label>
                    <input
                      type="text"
                      name="block"
                      value={formData.block}
                      onChange={handleInputChange}
                      className="input mt-1"
                      required
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700">
                      Floor
                    </label>
                    <input
                      type="number"
                      name="floor"
                      value={formData.floor}
                      onChange={handleInputChange}
                      className="input mt-1"
                      min="0"
                      required
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700">
                      Type
                    </label>
                    <select
                      name="type"
                      value={formData.type}
                      onChange={handleInputChange}
                      className="input mt-1"
                      required
                    >
                      <option value="">Select Type</option>
                      <option value="single">Single</option>
                      <option value="double">Double</option>
                      <option value="triple">Triple</option>
                    </select>
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700">
                      Capacity
                    </label>
                    <input
                      type="number"
                      name="capacity"
                      value={formData.capacity}
                      onChange={handleInputChange}
                      className="input mt-1"
                      min="1"
                      required
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700">
                      Monthly Rent (₹)
                    </label>
                    <input
                      type="number"
                      name="monthlyRent"
                      value={formData.monthlyRent}
                      onChange={handleInputChange}
                      className="input mt-1"
                      min="0"
                      step="0.01"
                      required
                    />
                  </div>
                  <div className="md:col-span-2">
                    <label className="block text-sm font-medium text-gray-700">
                      Security Deposit (₹)
                    </label>
                    <input
                      type="number"
                      name="securityDeposit"
                      value={formData.securityDeposit}
                      onChange={handleInputChange}
                      className="input mt-1"
                      min="0"
                      step="0.01"
                      required
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Amenities
                  </label>
                  <div className="grid grid-cols-2 md:grid-cols-4 gap-2">
                    {amenityOptions.map((amenity) => (
                      <label key={amenity} className="flex items-center">
                        <input
                          type="checkbox"
                          checked={formData.amenities.includes(amenity)}
                          onChange={() => handleAmenityChange(amenity)}
                          className="mr-2"
                        />
                        <span className="text-sm capitalize">
                          {amenity.replace("_", " ")}
                        </span>
                      </label>
                    ))}
                  </div>
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
                    rows="3"
                  />
                </div>

                <div className="flex justify-end space-x-3 pt-4">
                  <button
                    type="button"
                    onClick={() => setShowRoomForm(false)}
                    className="btn-outline"
                  >
                    Cancel
                  </button>
                  <button type="submit" className="btn-primary">
                    {editingRoom ? "Update Room" : "Create Room"}
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

export default Rooms;
