import request from '../request';
import { BaseResponse, PageData } from '../types';

// 评价信息
export interface Evaluation {
  id: number;
  orderId: number;        // 订单ID
  userId: number;         // 用户ID
  companionId: number;    // 陪诊员ID
  professionalismScore: number;  // 专业性评分
  attitudeScore: number;         // 态度评分
  efficiencyScore: number;       // 效率评分
  satisfactionScore: number;     // 满意度评分
  averageScore: number;          // 平均评分
  evaluationText?: string;       // 评价内容
  status: 'published' | 'draft' | 'hidden'; // 评价状态
  createTime: string;
  updateTime: string;
}

/**
 * 获取评价列表
 * @param current 当前页码
 * @param size 每页大小
 */
export function listEvaluations(
  current: number = 1,
  size: number = 10
): Promise<BaseResponse<PageData<Evaluation>>> {
  return request.get('/evaluation/list', {
    params: { current, size },
  }) as Promise<BaseResponse<PageData<Evaluation>>>;
}

/**
 * 获取评价详情
 * @param id 评价ID
 */
export function getEvaluation(id: number): Promise<BaseResponse<Evaluation>> {
  return request.get(`/evaluation/${id}`) as Promise<BaseResponse<Evaluation>>;
}

/**
 * 创建评价
 * @param evaluation 评价信息
 */
export function createEvaluation(evaluation: Partial<Evaluation>): Promise<BaseResponse<Evaluation>> {
  return request.post('/evaluation/create', evaluation) as Promise<BaseResponse<Evaluation>>;
}

/**
 * 更新评价
 * @param evaluation 评价信息
 */
export function updateEvaluation(evaluation: Partial<Evaluation> & { id: number }): Promise<BaseResponse<boolean>> {
  return request.post('/evaluation/update', evaluation) as Promise<BaseResponse<boolean>>;
}

/**
 * 删除评价
 * @param id 评价ID
 */
export function deleteEvaluation(id: number): Promise<BaseResponse<boolean>> {
  return request.post(`/evaluation/delete/${id}`) as Promise<BaseResponse<boolean>>;
}

/**
 * 获取陪诊员评价列表
 * @param companionId 陪诊员ID
 * @param current 当前页码
 * @param size 每页大小
 */
export function listEvaluationsByCompanion(
  companionId: number,
  current: number = 1,
  size: number = 10
): Promise<BaseResponse<PageData<Evaluation>>> {
  return request.get('/evaluation/list-by-companion', {
    params: { companionId, current, size },
  }) as Promise<BaseResponse<PageData<Evaluation>>>;
}
