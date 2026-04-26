import request from '../request';
import { BaseResponse, PageData, PageParams } from '../types';

export interface TrainingCourseItem {
  id?: number;
  title: string;
  category?: string;
  description?: string;
  content: string;
  difficulty?: number;
  duration?: number;
  instructor?: string;
  status?: number;
  createTime?: string;
  updateTime?: string;
}

export interface TrainingCourseListParams extends PageParams {
  keyword?: string;
  status?: number;
  category?: string;
}

export function listTrainingCourses(params: TrainingCourseListParams): Promise<BaseResponse<PageData<TrainingCourseItem>>> {
  return request.get('/training/admin/courses', { params }) as Promise<BaseResponse<PageData<TrainingCourseItem>>>;
}

export function createTrainingCourse(data: TrainingCourseItem): Promise<BaseResponse<TrainingCourseItem>> {
  return request.post('/training/admin/course', data) as Promise<BaseResponse<TrainingCourseItem>>;
}

export function updateTrainingCourse(id: number, data: Partial<TrainingCourseItem>): Promise<BaseResponse<string>> {
  return request.post(`/training/admin/course/${id}`, data) as Promise<BaseResponse<string>>;
}

export function publishTrainingCourse(id: number): Promise<BaseResponse<string>> {
  return request.post(`/training/admin/course/${id}/publish`) as Promise<BaseResponse<string>>;
}

export function unpublishTrainingCourse(id: number): Promise<BaseResponse<string>> {
  return request.post(`/training/admin/course/${id}/unpublish`) as Promise<BaseResponse<string>>;
}

export function deleteTrainingCourse(id: number): Promise<BaseResponse<string>> {
  return request.post(`/training/admin/course/${id}/delete`) as Promise<BaseResponse<string>>;
}
