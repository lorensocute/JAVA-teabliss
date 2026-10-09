import { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { toast } from '../components/Toast';
import { api } from '../services/api';
import './AuthPage.css';

export default function AuthPage() {
  const navigate = useNavigate();
  const location = useLocation();

  const [isLogin, setIsLogin] = useState(true);
  const [loading, setLoading] = useState(false);

  const [formData, setFormData] = useState({
    name: '',
    email: '',
    password: '',
    confirmPassword: '',
  });

  const [errors, setErrors] = useState({});

  useEffect(() => {
    if (location.pathname === '/register') {
      setIsLogin(false);
    } else {
      setIsLogin(true);
    }
  }, [location.pathname]);

  const handleChange = (e) => {
    const { name, value } = e.target;

    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));

    if (errors[name]) {
      setErrors((prev) => ({
        ...prev,
        [name]: null,
      }));
    }
  };

  const validateForm = () => {
    const newErrors = {};

    if (!isLogin && !formData.name.trim()) {
      newErrors.name = 'Vui lòng nhập họ và tên';
    }

    if (!formData.email.trim()) {
      newErrors.email = 'Vui lòng nhập email';
    } else if (!/\S+@\S+\.\S+/.test(formData.email)) {
      newErrors.email = 'Email không hợp lệ';
    } else if (/\s/.test(formData.email)) {
      newErrors.email = 'Email không được chứa khoảng trắng';
    }

    if (!formData.password) {
      newErrors.password = 'Vui lòng nhập mật khẩu';
    } else if (formData.password.length < 8) {
      newErrors.password = 'Mật khẩu phải có ít nhất 8 ký tự';
    } else if (/\s/.test(formData.password)) {
      newErrors.password = 'Mật khẩu không được chứa khoảng trắng';
    }

    if (!isLogin) {
      if (!formData.confirmPassword) {
        newErrors.confirmPassword =
          'Vui lòng xác nhận mật khẩu';
      } else if (
        formData.confirmPassword !== formData.password
      ) {
        newErrors.confirmPassword =
          'Mật khẩu xác nhận không khớp';
      }
    }

    setErrors(newErrors);

    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!validateForm()) {
      toast(
        'Vui lòng kiểm tra lại thông tin',
        'error'
      );
      return;
    }

    setLoading(true);

    try {
      if (isLogin) {
        const data = await api.login(
          formData.email.trim(),
          formData.password
        );

        /*
         * Backend dùng HttpSession.
         *
         * Không lưu JWT/token.
         * Cookie JSESSIONID được browser tự quản lý
         * nhờ credentials: 'include' trong api.js.
         *
         * localStorage chỉ lưu thông tin user để
         * frontend hiển thị giao diện.
         */
        localStorage.setItem(
          'user',
          JSON.stringify({
            id: data.id,
            fullName: data.fullName,
            email: data.email,
            role: data.role,
          })
        );

        // Xóa token cũ nếu trước đây frontend đã lưu
        localStorage.removeItem('token');

        window.dispatchEvent(new Event('userChanged'));

        toast(
          'Đăng nhập thành công!',
          'success'
        );

        /*
         * ADMIN vào trang quản trị.
         * CUSTOMER quay về trang chủ.
         */
        if (data.role === 'ADMIN') {
          navigate('/admin');
        } else {
          navigate('/');
        }

      } else {
        await api.register({
          fullName: formData.name.trim(),
          email: formData.email.trim(),
          password: formData.password,
        });

        toast(
          'Đăng ký thành công! Vui lòng đăng nhập.',
          'success'
        );

        setFormData({
          name: '',
          email: formData.email.trim(),
          password: '',
          confirmPassword: '',
        });

        setErrors({});
        setIsLogin(true);

        navigate(
          '/login',
          { replace: true }
        );
      }

    } catch (err) {
      toast(
        err.message ||
        'Có lỗi xảy ra, vui lòng thử lại',
        'error'
      );

    } finally {
      setLoading(false);
    }
  };

  const switchTab = (toLogin) => {
    setIsLogin(toLogin);
    setErrors({});

    setFormData((prev) => ({
      ...prev,
      password: '',
      confirmPassword: '',
    }));

    navigate(
      toLogin ? '/login' : '/register',
      { replace: true }
    );
  };

  return (
    <div className="auth-page">
      <div className="auth-container">

        <div className="auth-tabs">
          <button
            type="button"
            className={`auth-tab ${isLogin ? 'active' : ''
              }`}
            onClick={() => switchTab(true)}
          >
            Đăng nhập
          </button>

          <button
            type="button"
            className={`auth-tab ${!isLogin ? 'active' : ''
              }`}
            onClick={() => switchTab(false)}
          >
            Đăng ký
          </button>
        </div>

        <form
          className="auth-form"
          onSubmit={handleSubmit}
        >

          {!isLogin && (
            <div className="form-group">

              <label
                className="form-label"
                htmlFor="name"
              >
                Họ và tên
              </label>

              <input
                type="text"
                id="name"
                name="name"
                className={`form-input ${errors.name ? 'error' : ''
                  }`}
                value={formData.name}
                onChange={handleChange}
                placeholder="Nhập họ và tên của bạn"
              />

              {errors.name && (
                <span className="form-error">
                  {errors.name}
                </span>
              )}

            </div>
          )}

          <div className="form-group">

            <label
              className="form-label"
              htmlFor="email"
            >
              Email
            </label>

            <input
              type="email"
              id="email"
              name="email"
              className={`form-input ${errors.email ? 'error' : ''
                }`}
              value={formData.email}
              onChange={handleChange}
              placeholder="Nhập địa chỉ email"
            />

            {errors.email && (
              <span className="form-error">
                {errors.email}
              </span>
            )}

          </div>

          <div className="form-group">

            <label
              className="form-label"
              htmlFor="password"
            >
              Mật khẩu
            </label>

            <input
              type="password"
              id="password"
              name="password"
              className={`form-input ${errors.password ? 'error' : ''
                }`}
              value={formData.password}
              onChange={handleChange}
              placeholder="Nhập mật khẩu"
            />

            {errors.password && (
              <span className="form-error">
                {errors.password}
              </span>
            )}

          </div>

          {!isLogin && (
            <div className="form-group">

              <label
                className="form-label"
                htmlFor="confirmPassword"
              >
                Xác nhận mật khẩu
              </label>

              <input
                type="password"
                id="confirmPassword"
                name="confirmPassword"
                className={`form-input ${errors.confirmPassword
                    ? 'error'
                    : ''
                  }`}
                value={formData.confirmPassword}
                onChange={handleChange}
                placeholder="Nhập lại mật khẩu"
              />

              {errors.confirmPassword && (
                <span className="form-error">
                  {errors.confirmPassword}
                </span>
              )}

            </div>
          )}

          <button
            type="submit"
            className="btn btn-primary auth-button"
            disabled={loading}
          >
            {loading
              ? 'Đang xử lý...'
              : isLogin
                ? 'Đăng nhập'
                : 'Đăng ký'}
          </button>

        </form>

        <div className="auth-footer">

          {isLogin ? (
            <p>
              Chưa có tài khoản?{' '}

              <button
                type="button"
                onClick={() => switchTab(false)}
              >
                Đăng ký ngay
              </button>
            </p>
          ) : (
            <p>
              Đã có tài khoản?{' '}

              <button
                type="button"
                onClick={() => switchTab(true)}
              >
                Đăng nhập
              </button>
            </p>
          )}

        </div>

      </div>
    </div>
  );
}