import type {
  CustomCategory,
  GeneralCategory,
} from "@/src/entities/category/types/category";

export const generalCategories: readonly GeneralCategory[] = [
  {
    color: "#4CAF50",
    icon: "restaurant",
    id: "food",
    name: "Food & Dining",
  },
  {
    color: "#FF9800",
    icon: "shopping_bag",
    id: "shopping",
    name: "Shopping",
  },
  {
    color: "#2196F3",
    icon: "directions_car",
    id: "transportation",
    name: "Transportation",
  },
  {
    color: "#7E57C2",
    icon: "home",
    id: "housing",
    name: "Housing",
  },
  {
    color: "#00ACC1",
    icon: "bolt",
    id: "utilities",
    name: "Utilities",
  },
  {
    color: "#E91E63",
    icon: "medical_services",
    id: "healthcare",
    name: "Healthcare",
  },
  {
    color: "#3F51B5",
    icon: "movie",
    id: "entertainment",
    name: "Entertainment",
  },
  {
    color: "#795548",
    icon: "school",
    id: "education",
    name: "Education",
  },
  {
    color: "#009688",
    icon: "flight",
    id: "travel",
    name: "Travel",
  },
  {
    color: "#607D8B",
    icon: "more_horiz",
    id: "other",
    name: "Other",
  },
];

export const initialCustomCategories: readonly CustomCategory[] = [
  {
    generalCategoryId: "food",
    id: "groceries",
    name: "Groceries",
  },
  {
    generalCategoryId: "food",
    id: "restaurants",
    name: "Restaurants",
  },
  {
    generalCategoryId: "food",
    id: "coffee",
    name: "Coffee",
  },
  {
    generalCategoryId: "shopping",
    id: "clothing",
    name: "Clothing",
  },
  {
    generalCategoryId: "shopping",
    id: "electronics",
    name: "Electronics",
  },
  {
    generalCategoryId: "transportation",
    id: "fuel",
    name: "Fuel",
  },
  {
    generalCategoryId: "transportation",
    id: "public-transport",
    name: "Public Transport",
  },
  {
    generalCategoryId: "housing",
    id: "rent",
    name: "Rent",
  },
  {
    generalCategoryId: "utilities",
    id: "electricity",
    name: "Electricity",
  },
  {
    generalCategoryId: "healthcare",
    id: "pharmacy",
    name: "Pharmacy",
  },
];
