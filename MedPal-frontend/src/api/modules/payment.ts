import request from '../request';
import { BaseResponse, PageData } from '../types';

// 支付渠道
export type PaymentChannel = 'mock' | 'alipay' | 'wechat';

// 支付状态
export type PaymentStatus = 
  | 'unpaid'     // 未支付
  | 'pending'    // 待支付（兼容旧值）
  | 'processing' // 处理中
  | 'paid'       // 已支付
  | 'success'    // 支付成功（兼容旧值）
  | 'failed'     // 支付失败
  | 'cancelled'  // 已取消
  | 'refunded';  // 已退款

// 支付记录
export interface Payment {
  id: number;
  paymentNo: string;
  orderId: number;
  orderNo?: string;
  userId: number;
  amount: number;
  paymentChannel?: PaymentChannel | string;
  paymentStatus: PaymentStatus;
  paymentStatusText?: string;
  transactionId?: string;
  description?: string;
  paidTime?: string;
  expireTime?: string;
  voucherNo?: string;
  createTime: string;
  updateTime?: string;
}

// 支付 VO
export interface PaymentVO extends Payment {}

/**
 * 创建支付订单
 * @param orderId 订单ID
 * @param amount 支付金额
 * @param channel 支付渠道（默认mock）
 * @param description 支付描述
 */
export function createPayment(
  orderId: number,
  amount: number,
  channel: PaymentChannel = 'mock',
  description?: string
): Promise<BaseResponse<PaymentVO>> {
  return request.post('/payment/create', null, {
    params: { orderId, amount, channel, description },
  }) as Promise<BaseResponse<PaymentVO>>;
}

/**
 * 模拟支付确认
 * @param paymentId 支付ID
 * @param success 是否支付成功（默认true）
 */
export function mockPayment(paymentId: number, success: boolean = true): Promise<BaseResponse<PaymentVO>> {
  return request.post('/payment/mock-pay', null, {
    params: { paymentId, success },
  }) as Promise<BaseResponse<PaymentVO>>;
}

/**
 * 查询支付状态
 * @param orderId 订单ID
 */
export function getPaymentStatus(orderId: number): Promise<BaseResponse<PaymentVO>> {
  return request.get(`/payment/status/${orderId}`) as Promise<BaseResponse<PaymentVO>>;
}

/**
 * 获取支付详情
 * @param paymentId 支付ID
 */
export function getPaymentDetail(paymentId: number): Promise<BaseResponse<PaymentVO>> {
  return request.get(`/payment/detail/${paymentId}`) as Promise<BaseResponse<PaymentVO>>;
}

/**
 * 获取支付凭证
 * @param paymentId 支付ID
 */
export function getPaymentVoucher(paymentId: number): Promise<BaseResponse<PaymentVO>> {
  return request.get(`/payment/voucher/${paymentId}`) as Promise<BaseResponse<PaymentVO>>;
}

/**
 * 获取支付列表
 * @param current 当前页码
 * @param size 每页大小
 */
export function listPayments(
  current: number = 1,
  size: number = 10,
  keyword?: string,
  paymentStatus?: PaymentStatus
): Promise<BaseResponse<PageData<PaymentVO>>> {
  return request.get('/payment/list', {
    params: { current, size, keyword, paymentStatus },
  }) as Promise<BaseResponse<PageData<PaymentVO>>>;
}
