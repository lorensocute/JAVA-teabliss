import { useState, useEffect, useCallback } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { api } from '../services/api';
import { toast } from '../components/Toast';
import { getProductImage, handleImageError } from '../utils/imageHelper';
import './OrdersPage.css';

/* ────────────────────────────────────────────
   Constants
──────────────────────────────────────────── */
const STATUS_MAP = {
  PENDING:    { label: 'Chờ xác nhận',   icon: '⏳' },
  CONFIRMED:  { label: 'Đã xác nhận',    icon: '✅' },
  SHIPPING:   { label: 'Đang giao hàng', icon: '🚚' },
  DELIVERING: { label: 'Đang giao hàng', icon: '🚚' }, // alias backend
  COMPLETED:  { label: 'Hoàn thành',     icon: '🎉' },
  CANCELLED:  { label: 'Đã hủy',         icon: '❌' },
};

/** Map backend DrinkIce enum → hiển thị tiếng Việt */
const ICE_LABEL = {
  HOT: 'Nóng',
  LESS_ICE: 'Ít đá (30%)',
  NORMAL_ICE: 'Đá vừa (70%)',
  FULL_ICE: 'Đầy đá (100%)',
};

const SIZE_LABEL = { M: 'Size M', L: 'Size L', XL: 'Size XL' };

/* ────────────────────────────────────────────
   Helpers
──────────────────────────────────────────── */
const formatMoney = (value) =>
  new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value ?? 0);

const formatDate = (str) => {
  if (!str) return '—';
  return new Date(str).toLocaleString('vi-VN', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit',
  });
};

/* ────────────────────────────────────────────
   StatusBadge
──────────────────────────────────────────── */
function StatusBadge({ status }) {
  const info = STATUS_MAP[status] || { label: status, icon: '•' };
  // CSS class handles SHIPPING & DELIVERING together via --SHIPPING
  const cssStatus = status === 'DELIVERING' ? 'SHIPPING' : status;
  return (
    <span className={`order-status order-status--${cssStatus}`}>
      {info.icon} {info.label}
    </span>
  );
}

/* ────────────────────────────────────────────
   Order Detail Modal
──────────────────────────────────────────── */
function OrderDetailModal({ orderId, onClose }) {
  const [detail, setDetail] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setLoading(true);
    api.getMyOrderById(orderId)
      .then(setDetail)
      .catch((err) => {
        toast(err.message || 'Không thể tải chi tiết đơn hàng', 'error');
        onClose();
      })
      .finally(() => setLoading(false));
  }, [orderId, onClose]);

  // Close on Escape key
  useEffect(() => {
    const handler = (e) => { if (e.key === 'Escape') onClose(); };
    document.addEventListener('keydown', handler);
    return () => document.removeEventListener('keydown', handler);
  }, [onClose]);

  return (
    <div className="order-modal" role="dialog" aria-modal="true" aria-label="Chi tiết đơn hàng">
      <div className="order-modal__backdrop" onClick={onClose} />
      <div className="order-modal__box">
        <div className="order-modal__header">
          <span className="order-modal__title">
            {detail ? `Đơn hàng #${detail.id}` : 'Chi tiết đơn hàng'}
          </span>
          <button
            className="order-modal__close"
            onClick={onClose}
            aria-label="Đóng"
          >✕</button>
        </div>

        {loading ? (
          <div className="order-modal__loading">
            <div className="spinner" />
          </div>
        ) : detail ? (
          <div className="order-modal__body">

            {/* ── Thông tin chung ── */}
            <div className="order-detail-section">
              <div className="order-detail-section__title">Thông tin đơn hàng</div>
              <div className="order-detail-grid">
                <div className="order-detail-row">
                  <span className="order-detail-row__label">Mã đơn</span>
                  <span className="order-detail-row__value">#{detail.id}</span>
                </div>
                <div className="order-detail-row">
                  <span className="order-detail-row__label">Ngày đặt</span>
                  <span className="order-detail-row__value">{formatDate(detail.createdAt)}</span>
                </div>
                <div className="order-detail-row">
                  <span className="order-detail-row__label">Trạng thái</span>
                  <span className="order-detail-row__value">
                    <StatusBadge status={detail.status} />
                  </span>
                </div>
                <div className="order-detail-row">
                  <span className="order-detail-row__label">Thanh toán</span>
                  <span className="order-detail-row__value">
                    {detail.paymentMethod === 'COD' ? '💵 Tiền mặt khi nhận' : detail.paymentMethod}
                  </span>
                </div>
              </div>
            </div>

            {/* ── Thông tin giao hàng ── */}
            <div className="order-detail-section">
              <div className="order-detail-section__title">Thông tin giao hàng</div>
              <div className="order-detail-grid">
                <div className="order-detail-row">
                  <span className="order-detail-row__label">Người nhận</span>
                  <span className="order-detail-row__value">{detail.receiverName || '—'}</span>
                </div>
                <div className="order-detail-row">
                  <span className="order-detail-row__label">Số điện thoại</span>
                  <span className="order-detail-row__value">{detail.receiverPhone || '—'}</span>
                </div>
                <div className="order-detail-row order-detail-row--full">
                  <span className="order-detail-row__label">Địa chỉ giao hàng</span>
                  <span className="order-detail-row__value">{detail.shippingAddress || '—'}</span>
                </div>
              </div>
            </div>

            {/* ── Sản phẩm ── */}
            {Array.isArray(detail.items) && detail.items.length > 0 && (
              <div className="order-detail-section">
                <div className="order-detail-section__title">
                  Sản phẩm ({detail.items.length})
                </div>
                <div className="order-items-list">
                  {detail.items.map((item, idx) => {
                    // Defensive: render whatever fields backend actually returns
                    const name = item.productName || item.name || `Sản phẩm #${idx + 1}`;
                    const img  = getProductImage(item);
                    const qty  = item.quantity ?? 1;
                    const unit = item.unitPrice ?? item.price ?? 0;

                    // Build option string from optional fields
                    const opts = [
                      item.size && (SIZE_LABEL[item.size] || `Size ${item.size}`),
                      // sweetness: phải dùng !== null vì 0 là "Không ngọt" hợp lệ
                      item.sweetness !== null && item.sweetness !== undefined
                        && (item.sweetness === 0 ? 'Không ngọt (0%)' : `${item.sweetness}% ngọt`),
                      item.ice && (ICE_LABEL[item.ice] || item.ice),
                      item.toppings && (
                        Array.isArray(item.toppings)
                          ? (item.toppings.length > 0 ? `Topping: ${item.toppings.map(t => t.name || t.toppingName || t).join(', ')}` : null)
                          : item.toppings
                      ),
                    ].filter(Boolean).join(' · ');

                    const total = item.lineTotal ?? (unit * qty);

                    return (
                      <div key={item.id ?? idx} className="order-item-row">
                        {img ? (
                          <img
                            src={img}
                            alt={name}
                            className="order-item-row__img"
                            onError={handleImageError}
                          />
                        ) : (
                          <div className="order-item-row__img-placeholder">🧋</div>
                        )}
                        <div className="order-item-row__info">
                          <div className="order-item-row__name">{name}</div>
                          {opts && <div className="order-item-row__sub">{opts}</div>}
                        </div>
                        <div className="order-item-row__qty">x{qty}</div>
                        <div className="order-item-row__price">{formatMoney(total)}</div>
                      </div>
                    );
                  })}
                </div>

                {/* Totals */}
                <div className="order-totals">
                  {detail.subtotal != null && (
                    <div className="order-totals__row">
                      <span>Tạm tính</span>
                      <span>{formatMoney(detail.subtotal)}</span>
                    </div>
                  )}
                  {detail.discountAmount != null && detail.discountAmount > 0 && (
                    <div className="order-totals__row order-totals__row--discount">
                      <span>Giảm giá</span>
                      <span>-{formatMoney(detail.discountAmount)}</span>
                    </div>
                  )}
                  <div className="order-totals__grand">
                    <span>Tổng thanh toán</span>
                    <span className="order-totals__grand-value">{formatMoney(detail.totalAmount)}</span>
                  </div>
                </div>
              </div>
            )}

            {/* Fallback if no items array from backend */}
            {(!detail.items || detail.items.length === 0) && (
              <div className="order-detail-section">
                <div className="order-detail-section__title">Tổng thanh toán</div>
                <div className="order-totals">
                  {detail.subtotal != null && (
                    <div className="order-totals__row">
                      <span>Tạm tính</span>
                      <span>{formatMoney(detail.subtotal)}</span>
                    </div>
                  )}
                  {detail.discountAmount != null && detail.discountAmount > 0 && (
                    <div className="order-totals__row order-totals__row--discount">
                      <span>Giảm giá</span>
                      <span>-{formatMoney(detail.discountAmount)}</span>
                    </div>
                  )}
                  <div className="order-totals__grand">
                    <span>Tổng thanh toán</span>
                    <span className="order-totals__grand-value">{formatMoney(detail.totalAmount)}</span>
                  </div>
                </div>
              </div>
            )}

          </div>
        ) : null}
      </div>
    </div>
  );
}

/* ────────────────────────────────────────────
   OrdersPage main
──────────────────────────────────────────── */
export default function OrdersPage() {
  const navigate = useNavigate();

  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedOrderId, setSelectedOrderId] = useState(null);

  const loadOrders = useCallback(async () => {
    setLoading(true);
    try {
      const data = await api.getMyOrders();
      setOrders(Array.isArray(data) ? data : []);
    } catch (err) {
      // 401 / chưa đăng nhập
      const localUser = (() => {
        try { return JSON.parse(localStorage.getItem('user')); } catch { return null; }
      })();
      if (!localUser) {
        navigate('/login');
        return;
      }
      toast(err.message || 'Không thể tải danh sách đơn hàng', 'error');
      navigate('/login');
    } finally {
      setLoading(false);
    }
  }, [navigate]);

  useEffect(() => {
    const localUser = (() => {
      try { return JSON.parse(localStorage.getItem('user')); } catch { return null; }
    })();
    if (!localUser) { navigate('/login'); return; }
    loadOrders();
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  useEffect(() => {
    document.title = 'Đơn hàng của tôi | TeaBliss';
    return () => { document.title = 'TeaBliss'; };
  }, []);

  /* ── Loading state ── */
  if (loading) {
    return (
      <div className="orders-page">
        <div className="orders-loading">
          <div className="spinner" />
          <p style={{ color: 'var(--color-text-muted)', fontSize: '0.9rem' }}>
            Đang tải đơn hàng...
          </p>
        </div>
      </div>
    );
  }

  return (
    <div className="orders-page">
      <div className="orders-page__inner">

        {/* ─── Heading ─── */}
        <div className="orders-page__heading">
          <h1>📦 Đơn hàng của tôi</h1>
          <p>Theo dõi trạng thái và lịch sử đặt hàng của bạn</p>
        </div>

        {/* ─── Empty state ─── */}
        {orders.length === 0 ? (
          <div className="orders-empty">
            <div className="empty-state">
              <div className="empty-state__icon">🛒</div>
              <h2 className="empty-state__title">Chưa có đơn hàng nào</h2>
              <p className="empty-state__desc">
                Bạn chưa đặt hàng lần nào. Hãy khám phá thực đơn và thưởng thức trà ngay!
              </p>
              <Link
                to="/products"
                className="btn btn-primary"
                id="go-shop-from-orders"
                style={{ marginTop: 'var(--space-md)' }}
              >
                Khám phá Thực Đơn
              </Link>
            </div>
          </div>
        ) : (
          /* ─── Order list ─── */
          <div>
            {orders.map((order, idx) => (
              <div
                key={order.id}
                className="order-card"
                style={{ animationDelay: `${idx * 0.05}s` }}
              >
                {/* Header row */}
                <div className="order-card__header">
                  <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-md)', flexWrap: 'wrap' }}>
                    <span className="order-card__id">Đơn #{order.id}</span>
                    <span className="order-card__date">{formatDate(order.createdAt)}</span>
                  </div>
                  <StatusBadge status={order.status} />
                </div>

                {/* Body row */}
                <div className="order-card__body">
                  <div className="order-card__meta">
                    {Array.isArray(order.items) && order.items.length > 0 && (
                      <div className="order-card__items-preview">
                        <span className="order-card__items-count">
                          🧋 {order.items.reduce((sum, it) => sum + (it.quantity || 1), 0)} món:
                        </span>
                        <span className="order-card__items-names">
                          {order.items.map(it => `${it.productName || 'Sản phẩm'} (x${it.quantity || 1})`).join(', ')}
                        </span>
                      </div>
                    )}
                    <div className="order-card__payment">
                      💵 {order.paymentMethod === 'COD' ? 'Thanh toán khi nhận hàng' : (order.paymentMethod || 'COD')}
                    </div>
                    <div>
                      <div className="order-card__total-label">Tổng tiền</div>
                      <div className="order-card__total-value">{formatMoney(order.totalAmount)}</div>
                    </div>
                  </div>

                  <div className="order-card__actions">
                    <button
                      className="btn btn-ghost btn-sm"
                      id={`view-order-${order.id}`}
                      onClick={() => setSelectedOrderId(order.id)}
                    >
                      Xem chi tiết →
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}

      </div>

      {/* ─── Detail Modal ─── */}
      {selectedOrderId !== null && (
        <OrderDetailModal
          orderId={selectedOrderId}
          onClose={() => setSelectedOrderId(null)}
        />
      )}
    </div>
  );
}
