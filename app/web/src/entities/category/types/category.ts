export type CategoryId = string;

export type GeneralCategory = {
  color: string;
  icon: string;
  id: CategoryId;
  name: string;
};

export type CustomCategory = {
  generalCategoryId: CategoryId;
  id: CategoryId;
  name: string;
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
