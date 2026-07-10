export type BudgetLimit = {
  customCategoryId: string | null;
  generalCategoryId: string;
  id: number;
  monthlyLimit: number;
  period: string;
  userId: number;
};

export type BudgetLimitInput = {
  customCategoryId?: string | null;
  generalCategoryId: string;
  monthlyLimit: number;
  period: string;
  userId: number;
};
