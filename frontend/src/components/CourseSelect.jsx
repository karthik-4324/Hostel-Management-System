const COURSE_GROUPS = [
  {
    label: "Engineering",
    options: ["CSE", "CSE (AI)", "AIML", "ME", "CE", "EEE", "ECE", "IT"],
  },
  {
    label: "Pharmacy",
    options: ["Pharmacy"],
  },
  {
    label: "Diploma",
    options: [
      "Diploma - CSE",
      "Diploma - ME",
      "Diploma - CE",
      "Diploma - EEE",
      "Diploma - ECE",
    ],
  },
];

const ALL_COURSES = COURSE_GROUPS.flatMap((group) => group.options);

const CourseSelect = ({
  id = "course",
  name = "course",
  value,
  onChange,
  required = false,
  className = "input mt-1",
}) => (
  <select
    id={id}
    name={name}
    value={value || ""}
    onChange={onChange}
    required={required}
    className={className}
  >
    <option value="">Select Course</option>
    {/* keeps older saved values (like "Cse") visible instead of blank */}
    {value && !ALL_COURSES.includes(value) && (
      <option value={value}>{value}</option>
    )}
    {COURSE_GROUPS.map((group) => (
      <optgroup key={group.label} label={group.label}>
        {group.options.map((option) => (
          <option key={option} value={option}>
            {option}
          </option>
        ))}
      </optgroup>
    ))}
  </select>
);

export default CourseSelect;
