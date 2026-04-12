const STORAGE_KEYS = {
  seeded: 'medpal_seeded_v1',
  user: 'medpal_user',
  orders: 'medpal_orders',
  notifications: 'medpal_notifications',
  records: 'medpal_records'
};

const ORDER_STATUS_MAP = {
  pending: { label: '待接单', color: '#ff9f1a' },
  confirmed: { label: '已接单', color: '#3c7dff' },
  serving: { label: '服务中', color: '#2ac769' },
  completed: { label: '已完成', color: '#7b8291' },
  cancelled: { label: '已取消', color: '#ff5c5c' }
};

const seed = {
  user: {
    id: 10001,
    userName: '王小雨',
    userAccount: 'medpal_user',
    userPhone: '13800000000',
    userEmail: 'user@medpal.com',
    userRole: 'user',
    userAvatar: '',
    gender: '女',
    birthDate: '1995-06-18',
    address: '上海市 徐汇区 漕溪北路 18 号'
  },
  hospitals: [
    {
      id: 1,
      hospitalName: '仁和医疗中心',
      hospitalLevel: '三级甲等',
      address: '上海市徐汇区枫林路 180 号',
      phone: '021-88886666',
      introduction: '集医疗、科研、教学于一体的综合性医院。',
      features: ['心内科', '神经科', '综合急诊'],
      bookingCount: 1240,
      departmentCount: 23,
      distance: 2.4,
      rating: 4.8
    },
    {
      id: 2,
      hospitalName: '安康医院',
      hospitalLevel: '三级乙等',
      address: '上海市浦东新区锦绣路 99 号',
      phone: '021-66668888',
      introduction: '专注老年医学与康复治疗的重点医院。',
      features: ['康复科', '老年病', '影像中心'],
      bookingCount: 860,
      departmentCount: 18,
      distance: 5.1,
      rating: 4.6
    },
    {
      id: 3,
      hospitalName: '长宁协和医院',
      hospitalLevel: '二级甲等',
      address: '上海市长宁区天山路 318 号',
      phone: '021-53332222',
      introduction: '区域内综合医疗服务与社区健康管理中心。',
      features: ['妇产科', '儿科', '慢病管理'],
      bookingCount: 540,
      departmentCount: 12,
      distance: 3.2,
      rating: 4.5
    }
  ],
  departments: {
    1: [
      { id: 101, departmentName: '心内科', doctorCount: 12 },
      { id: 102, departmentName: '呼吸内科', doctorCount: 10 },
      { id: 103, departmentName: '神经内科', doctorCount: 9 }
    ],
    2: [
      { id: 201, departmentName: '康复科', doctorCount: 7 },
      { id: 202, departmentName: '老年病科', doctorCount: 8 }
    ],
    3: [
      { id: 301, departmentName: '妇产科', doctorCount: 6 },
      { id: 302, departmentName: '儿科', doctorCount: 5 }
    ]
  },
  doctors: {
    101: [
      { id: 1001, doctorName: '张远航', title: '主任医师', specialty: '心血管疾病', scheduleTime: '周二/周五 上午', registrationFee: 120, rating: 4.8 },
      { id: 1002, doctorName: '李瑞', title: '副主任医师', specialty: '高血压管理', scheduleTime: '周一/周四 下午', registrationFee: 80, rating: 4.6 }
    ],
    102: [
      { id: 1003, doctorName: '王敏', title: '主任医师', specialty: '慢阻肺', scheduleTime: '周三 上午', registrationFee: 100, rating: 4.7 }
    ],
    201: [
      { id: 2001, doctorName: '陈雪', title: '主治医师', specialty: '术后康复', scheduleTime: '周一至周五 上午', registrationFee: 60, rating: 4.6 }
    ],
    301: [
      { id: 3001, doctorName: '赵怡', title: '主任医师', specialty: '高危妊娠', scheduleTime: '周二/周四 上午', registrationFee: 110, rating: 4.8 }
    ]
  },
  companions: [
    {
      id: 11,
      companionName: '刘晨',
      specialties: '慢病陪诊 · 心内科',
      rating: 4.8,
      serviceCount: 158,
      workYears: 5,
      serviceArea: '徐汇 / 长宁',
      totalIncome: 52000,
      realNameStatus: 'approved',
      qualificationStatus: 'approved',
      qualificationType: '护士资格',
      companionPhone: '13811112222',
      companionAccount: 'lc_companion'
    },
    {
      id: 12,
      companionName: '周琪',
      specialties: '产检陪诊 · 妇产科',
      rating: 4.6,
      serviceCount: 96,
      workYears: 4,
      serviceArea: '浦东 / 徐汇',
      totalIncome: 41000,
      realNameStatus: 'approved',
      qualificationStatus: 'pending',
      qualificationType: '护理证书',
      companionPhone: '13822223333',
      companionAccount: 'zq_companion'
    },
    {
      id: 13,
      companionName: '马晓宇',
      specialties: '老年陪诊 · 康复护理',
      rating: 4.7,
      serviceCount: 121,
      workYears: 6,
      serviceArea: '浦东 / 杨浦',
      totalIncome: 61000,
      realNameStatus: 'approved',
      qualificationStatus: 'approved',
      qualificationType: '康复技师',
      companionPhone: '13833334444',
      companionAccount: 'mxy_companion'
    }
  ],
  evaluations: {
    11: [
      {
        id: 9001,
        evaluationText: '陪诊过程很专业，沟通清晰。',
        averageScore: 4.8,
        professionalismScore: 5,
        attitudeScore: 5,
        efficiencyScore: 4,
        satisfactionScore: 5
      },
      {
        id: 9002,
        evaluationText: '耐心细致，安排得很到位。',
        averageScore: 4.7,
        professionalismScore: 5,
        attitudeScore: 4,
        efficiencyScore: 5,
        satisfactionScore: 5
      }
    ],
    12: [
      {
        id: 9003,
        evaluationText: '陪诊沟通很安心。',
        averageScore: 4.6,
        professionalismScore: 4,
        attitudeScore: 5,
        efficiencyScore: 4,
        satisfactionScore: 5
      }
    ],
    13: []
  },
  notifications: [
    {
      id: 7001,
      title: '订单已接单',
      content: '您的订单已被陪诊员接单，点击查看详情。',
      status: 'unread',
      createTime: Date.now() - 1000 * 60 * 30
    },
    {
      id: 7002,
      title: '陪诊提醒',
      content: '明天 9:00 有陪诊服务，请提前准备资料。',
      status: 'read',
      createTime: Date.now() - 1000 * 60 * 60 * 5
    },
    {
      id: 7003,
      title: '健康科普',
      content: '春季养护指南已上线，快来看看吧。',
      status: 'unread',
      createTime: Date.now() - 1000 * 60 * 60 * 24 * 2
    }
  ],
  records: [
    {
      id: 8001,
      hospitalName: '仁和医疗中心',
      departmentName: '心内科',
      doctorName: '张远航',
      visitDate: '2025-10-12',
      recordNo: 'MH20251012001',
      diagnosis: '高血压',
      symptoms: '胸闷、头晕',
      checkResults: '心电图异常',
      treatment: '药物控制血压',
      prescription: '氨氯地平 5mg/日',
      doctorAdvice: '规律作息，低盐饮食',
      attachments: '心电图报告.pdf'
    },
    {
      id: 8002,
      hospitalName: '安康医院',
      departmentName: '康复科',
      doctorName: '陈雪',
      visitDate: '2025-08-03',
      recordNo: 'MH20250803018',
      diagnosis: '膝关节术后康复',
      symptoms: '膝部酸痛',
      checkResults: '肌力不足',
      treatment: '康复训练',
      prescription: '康复课程 8 次',
      doctorAdvice: '避免剧烈运动',
      attachments: ''
    }
  ],
  orders: [
    {
      id: 5001,
      orderStatus: 'pending',
      paymentStatus: 'unpaid',
      hospitalId: 1,
      hospitalName: '仁和医疗中心',
      departmentName: '心内科',
      doctorName: '张远航',
      appointmentDate: '2026-02-12 09:00',
      createTime: '2026-02-05 09:20',
      duration: 2,
      specificNeeds: '代取报告',
      serviceFee: 200,
      platformFee: 20,
      totalPrice: 220,
      companionId: 11,
      companionName: '刘晨',
      companionPhone: '13811112222'
    },
    {
      id: 5002,
      orderStatus: 'completed',
      paymentStatus: 'paid',
      hospitalId: 2,
      hospitalName: '安康医院',
      departmentName: '康复科',
      doctorName: '陈雪',
      appointmentDate: '2025-12-15 14:00',
      createTime: '2025-12-01 11:15',
      duration: 4,
      specificNeeds: '轮椅陪同',
      serviceFee: 400,
      platformFee: 40,
      totalPrice: 440,
      companionId: 13,
      companionName: '马晓宇',
      companionPhone: '13833334444'
    }
  ]
};

const clone = (data) => JSON.parse(JSON.stringify(data));

const ensureSeed = () => {
  const seeded = uni.getStorageSync(STORAGE_KEYS.seeded);
  if (seeded) return;

  uni.setStorageSync(STORAGE_KEYS.user, clone(seed.user));
  uni.setStorageSync(STORAGE_KEYS.orders, clone(seed.orders));
  uni.setStorageSync(STORAGE_KEYS.notifications, clone(seed.notifications));
  uni.setStorageSync(STORAGE_KEYS.records, clone(seed.records));
  uni.setStorageSync(STORAGE_KEYS.seeded, true);
};

const getUser = () => {
  ensureSeed();
  return uni.getStorageSync(STORAGE_KEYS.user);
};

const setUser = (user) => {
  uni.setStorageSync(STORAGE_KEYS.user, user);
  return user;
};

const clearUser = () => {
  uni.removeStorageSync(STORAGE_KEYS.user);
};

const getHospitals = () => clone(seed.hospitals);

const getHospitalById = (id) => seed.hospitals.find((item) => item.id === Number(id)) || null;

const getDepartments = (hospitalId) => clone(seed.departments[hospitalId] || []);

const getDoctors = (departmentId) => clone(seed.doctors[departmentId] || []);

const getCompanions = () => clone(seed.companions);

const getCompanionById = (id) => seed.companions.find((item) => item.id === Number(id)) || null;

const getEvaluations = (companionId) => clone(seed.evaluations[companionId] || []);

const getOrders = () => {
  ensureSeed();
  return uni.getStorageSync(STORAGE_KEYS.orders) || [];
};

const getOrderById = (id) => {
  const orders = getOrders();
  return orders.find((order) => order.id === Number(id)) || null;
};

const addOrder = (order) => {
  const orders = getOrders();
  orders.unshift(order);
  uni.setStorageSync(STORAGE_KEYS.orders, orders);
  return order;
};

const updateOrder = (orderId, patch) => {
  const orders = getOrders();
  const index = orders.findIndex((order) => order.id === Number(orderId));
  if (index === -1) return null;
  const nextOrder = { ...orders[index], ...patch };
  orders.splice(index, 1, nextOrder);
  uni.setStorageSync(STORAGE_KEYS.orders, orders);
  return nextOrder;
};

const getNotifications = () => {
  ensureSeed();
  return uni.getStorageSync(STORAGE_KEYS.notifications) || [];
};

const setNotifications = (list) => {
  uni.setStorageSync(STORAGE_KEYS.notifications, list);
  return list;
};

const getRecords = () => {
  ensureSeed();
  return uni.getStorageSync(STORAGE_KEYS.records) || [];
};

const getRecordById = (id) => {
  const records = getRecords();
  return records.find((record) => record.id === Number(id)) || null;
};

const getStatusText = (status) => (ORDER_STATUS_MAP[status] ? ORDER_STATUS_MAP[status].label : '未知');

const getStatusColor = (status) => (ORDER_STATUS_MAP[status] ? ORDER_STATUS_MAP[status].color : '#7b8291');

export {
  ORDER_STATUS_MAP,
  addOrder,
  clearUser,
  getCompanionById,
  getCompanions,
  getDepartments,
  getDoctors,
  getEvaluations,
  getHospitalById,
  getHospitals,
  getNotifications,
  getOrderById,
  getOrders,
  getRecordById,
  getRecords,
  getStatusColor,
  getStatusText,
  getUser,
  setNotifications,
  setUser,
  updateOrder
};
