export type CategoryId = string;

export type GeneralCategory = {
  color: string;
  code: string;
  icon: string;
  id: CategoryId;
  name: string;
};

export type CustomCategory = {
  createdAt?: string;
  generalCategoryCode?: string;
  generalCategoryId: CategoryId;
  generalCategoryName?: string;
  id: CategoryId;
  name: string;
  updatedAt?: string;
  userId?: number;
};

export type CreateCustomCategoryInput = {
  generalCategoryId: CategoryId;
  name: string;
};

export type UpdateCustomCategoryInput = {
  generalCategoryId?: CategoryId;
  id: CategoryId;
  name?: string;
};
