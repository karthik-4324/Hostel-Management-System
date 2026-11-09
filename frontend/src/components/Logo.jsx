const Logo = ({ className = "h-12 w-12" }) => (
  <svg
    xmlns="http://www.w3.org/2000/svg"
    viewBox="0 0 640 480"
    className={className}
    fill="currentColor"
    role="img"
    aria-label="Hostel Management logo"
  >
    <path d="M72 186 320 38l96 64V57h42v73l110 56L320 86 72 186Z" />
    <path d="M132 198h42v133h292v-95h42v178h-42v-41H174v41h-42V198Z" />
    <circle cx="221" cy="258" r="31" />
    <rect x="184" y="298" width="74" height="28" rx="4" />
    <path d="M270 257h120c35.35 0 64 28.65 64 64v5H270v-69Z" />
  </svg>
);

export default Logo;
