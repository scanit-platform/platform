import { z } from "zod";
import { customCategoryNameMaxLength } from "@/src/entities/category/model/category-constraints";
import type {
  CategoryId,
  CustomCategory,
} from "@/src/entities/category/types/category";

type CustomCategorySchemaOptions = {
  customCategories: readonly CustomCategory[];
  excludedCustomCategoryId?: CategoryId;
  maxNameLength?: number;
};

const requiredNameMessage = "Category name is required.";
const duplicateNameMessage =
  "A custom category with this name already exists in this general category.";

function normalizeCategoryName(name: string) {
  return name.trim().toLocaleLowerCase();
}

function hasDuplicateCustomCategoryName({
  customCategories,
  excludedCustomCategoryId,
  generalCategoryId,
  name,
}: CustomCategorySchemaOptions & {
  generalCategoryId: CategoryId;
  name: string;
}) {
  const normalizedName = normalizeCategoryName(name);

  return customCategories.some((category) => {
    return (
      category.generalCategoryId === generalCategoryId &&
      category.id !== excludedCustomCategoryId &&
      normalizeCategoryName(category.name) === normalizedName
    );
  });
}

export function createCustomCategorySchema({
  customCategories,
  maxNameLength = customCategoryNameMaxLength,
}: CustomCategorySchemaOptions) {
  return z
    .object({
      generalCategoryId: z.string().trim().min(1, "Choose a general category."),
      name: z
        .string()
        .trim()
        .min(1, requiredNameMessage)
        .max(
          maxNameLength,
          `Category name must be ${maxNameLength} characters or fewer.`,
        ),
    })
    .superRefine((value, context) => {
      if (
        hasDuplicateCustomCategoryName({
          customCategories,
          generalCategoryId: value.generalCategoryId,
          name: value.name,
        })
      ) {
        context.addIssue({
          code: "custom",
          message: duplicateNameMessage,
          path: ["name"],
        });
      }
    });
}

export function updateCustomCategorySchema({
  customCategories,
  excludedCustomCategoryId,
  maxNameLength = customCategoryNameMaxLength,
}: CustomCategorySchemaOptions) {
  return z
    .object({
      generalCategoryId: z.string().trim().min(1, "Choose a general category."),
      id: z.string().trim().min(1, "Category id is required."),
      name: z
        .string()
        .trim()
        .min(1, requiredNameMessage)
        .max(
          maxNameLength,
          `Category name must be ${maxNameLength} characters or fewer.`,
        ),
    })
    .superRefine((value, context) => {
      if (
        hasDuplicateCustomCategoryName({
          customCategories,
          excludedCustomCategoryId,
          generalCategoryId: value.generalCategoryId,
          name: value.name,
        })
      ) {
        context.addIssue({
          code: "custom",
          message: duplicateNameMessage,
          path: ["name"],
        });
      }
    });
}

export type CreateCustomCategoryFormValues = z.infer<
  ReturnType<typeof createCustomCategorySchema>
>;

export type UpdateCustomCategoryFormValues = z.infer<
  ReturnType<typeof updateCustomCategorySchema>
>;
