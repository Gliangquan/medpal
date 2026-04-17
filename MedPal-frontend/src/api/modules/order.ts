import request from '../request';
import { BaseResponse, PageData } from '../types';

// 订单状态
export type OrderStatus = 
  | 'draft'      // 草稿
  | 'pending'    // 待接单
  | 'accepted'   // 已接单
  | 'confirmed'  // 已接单
  | 'serving'    // 服务中
  | 'completion_pending' // 待用户确认
  | 'rejected'   // 已拒单
  | 'in_progress'// 服务中
  | 'completed'  // 已完成
  | 'cancelled'; // 已取消

// 订单信息
export interface AppointmentOrder {
  id: number;
  orderNo: string;
  userId: number;
  companionId?: number;
  hospitalId: number;
  departmentId?: number;
  doctorId?: number;
  hospitalName?: string;
  departmentName?: string;
  doctorName?: string;
  companionName?: string;
  companionPhone?: string;
  patientPhone?: string;
  appointmentDate: string;
  duration?: string;
  specificNeeds?: string;
  totalPrice?: number;
  serviceFee?: number;
  extraFee?: number;
  platformFee?: number;
  paymentStatus?: string;
  orderStatus: OrderStatus;
  cancelReason?: string;
  completionRequestedTime?: string;
  createTime: string;
  updateTime: string;
}

/**
 * 创建订单
 * @param order 订单信息
 */
export function createOrder(order: Partial<AppointmentOrder>): Promise<BaseResponse<AppointmentOrder>> {
  return request.post('/order/create', order) as Promise<BaseResponse<AppointmentOrder>>;
}

/**
 * 保存订单草稿
 * @param order 订单信息
 */
export function saveDraft(order: Partial<AppointmentOrder>): Promise<BaseResponse<AppointmentOrder>> {
  return request.post('/order/draft/save', order) as Promise<BaseResponse<AppointmentOrder>>;
}

/**
 * 获取用户草稿列表
 * @param userId 用户ID
 * @param current 当前页码
 * @param size 每页大小
 */
export function listDrafts(
  userId: number,
  current: number = 1,
  size: number = 10
): Promise<BaseResponse<PageData<AppointmentOrder>>> {
  return request.get('/order/draft/list', {
    params: { userId, current, size },
  }) as Promise<BaseResponse<PageData<AppointmentOrder>>>;
}

/**
 * 提交草稿为正式订单
 * @param id 草稿ID
 */
export function submitDraft(id: number): Promise<BaseResponse<AppointmentOrder>> {
  return request.post(`/order/draft/submit/${id}`) as Promise<BaseResponse<AppointmentOrder>>;
}

/**
 * 获取用户订单列表
 * @param current 当前页码
 * @param size 每页大小
 * @param userId 用户ID（可选）
 * @param orderStatus 订单状态（可选）
 */
export function listOrders(
  current: number = 1,
  size: number = 10,
  userId?: number,
  orderStatus?: OrderStatus,
  keyword?: string
): Promise<BaseResponse<PageData<AppointmentOrder>>> {
  return request.get('/order/list', {
    params: { current, size, userId, orderStatus, keyword },
  }) as Promise<BaseResponse<PageData<AppointmentOrder>>>;
}

/**
 * 获取陪诊员订单列表
 * @param current 当前页码
 * @param size 每页大小
 * @param companionId 陪诊员ID
 * @param orderStatus 订单状态（可选）
 */
export function listOrdersByCompanion(
  current: number,
  size: number,
  companionId: number,
  orderStatus?: OrderStatus
): Promise<BaseResponse<PageData<AppointmentOrder>>> {
  return request.get('/order/list-by-companion', {
    params: { current, size, companionId, orderStatus },
  }) as Promise<BaseResponse<PageData<AppointmentOrder>>>;
}

/**
 * 获取订单详情
 * @param id 订单ID
 */
export function getOrder(id: number): Promise<BaseResponse<AppointmentOrder>> {
  return request.get(`/order/${id}`) as Promise<BaseResponse<AppointmentOrder>>;
}

/**
 * 接单
 * @param id 订单ID
 * @param companionId 陪诊员ID
 */
export function acceptOrder(id: number, companionId: number): Promise<BaseResponse<string>> {
  return request.post(`/order/accept/${id}`, null, {
    params: { companionId },
  }) as Promise<BaseResponse<string>>;
}

/**
 * 拒单
 * @param id 订单ID
 * @param reason 拒单原因
 */
export function rejectOrder(id: number, reason: string): Promise<BaseResponse<string>> {
  return request.post(`/order/reject/${id}`, null, {
    params: { reason },
  }) as Promise<BaseResponse<string>>;
}

/**
 * 完成订单
 * @param id 订单ID
 */
export function completeOrder(id: number): Promise<BaseResponse<string>> {
  return request.post(`/order/complete/${id}`) as Promise<BaseResponse<string>>;
}

/**
 * 用户确认完成订单
 * @param id 订单ID
 */
export function confirmCompleteOrder(id: number): Promise<BaseResponse<string>> {
  return request.post(`/order/confirm-complete/${id}`) as Promise<BaseResponse<string>>;
}

/**
 * 取消订单
 * @param id 订单ID
 * @param reason 取消原因
 */
export function cancelOrder(id: number, reason: string): Promise<BaseResponse<string>> {
  return request.post(`/order/cancel/${id}`, null, {
    params: { reason },
  }) as Promise<BaseResponse<string>>;
}

/**
 * 更新订单状态
 * @param id 订单ID
 * @param orderStatus 订单状态
 */
export function updateOrderStatus(id: number, orderStatus: OrderStatus): Promise<BaseResponse<string>> {
  return request.post(`/order/update-status/${id}`, null, {
    params: { orderStatus },
  }) as Promise<BaseResponse<string>>;
}
