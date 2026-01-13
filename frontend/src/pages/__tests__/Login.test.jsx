import '@testing-library/jest-dom';
import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import React from 'react';
import { BrowserRouter, useLocation, useNavigate } from 'react-router-dom';
import { AuthContext } from '../../context/AuthContext';
import Login from '../Login';

// Mock react-router-dom hooks
jest.mock('react-router-dom', () => ({
    ...jest.requireActual('react-router-dom'),
    useNavigate: jest.fn(),
    useLocation: jest.fn(),
}));

// Mock AuthContext
jest.mock('../../context/AuthContext', () => ({
    useAuth: () => ({
        login: jest.fn(),
        register: jest.fn(),
    }),
}));

const mockLogin = jest.fn();
const mockRegister = jest.fn();
const mockNavigate = jest.fn();
const mockLocation = {
    search: '',
    pathname: '/login',
    state: null,
    hash: '',
    key: 'default',
};

// Helper to render the component with AuthContext
const renderWithProviders = (ui, {
    providerProps,
    routerProps = { initialEntries: ['/login'] },
} = {}) => {
    return render(
        <BrowserRouter {...routerProps}>
            <AuthContext.Provider value={providerProps}>
                {ui}
            </AuthContext.Provider>
        </BrowserRouter>
    );
};

describe('Login Component', () => {
    beforeEach(() => {
        // Reset mocks before each test
        mockLogin.mockClear();
        mockRegister.mockClear();
        mockNavigate.mockClear();
        useNavigate.mockReturnValue(mockNavigate);
        useLocation.mockReturnValue(mockLocation);
        // Mock localStorage
        Object.defineProperty(window, 'localStorage', {
            value: {
                setItem: jest.fn(),
                getItem: jest.fn(),
                removeItem: jest.fn(),
                clear: jest.fn(),
            },
            writable: true,
        });
    });

    test('renders login form initially', () => {
        renderWithProviders(<Login />, {
            providerProps: { login: mockLogin, register: mockRegister }
        });

        expect(screen.getByRole('heading', { name: /Login/i })).toBeInTheDocument();
        expect(screen.getByPlaceholderText(/tusharseth@example.com/i)).toBeInTheDocument();
        expect(screen.getByPlaceholderText(/••••••••/i)).toBeInTheDocument();
        expect(screen.getByRole('button', { name: /Login/i })).toBeInTheDocument();
        expect(screen.queryByPlaceholderText(/Tushar Seth/i)).not.toBeInTheDocument(); // Name field should not be present
    });

    test('switches to register form', () => {
        renderWithProviders(<Login />, {
            providerProps: { login: mockLogin, register: mockRegister }
        });

        fireEvent.click(screen.getByRole('button', { name: /Register/i }));

        expect(screen.getByRole('heading', { name: /Register/i })).toBeInTheDocument();
        expect(screen.getByPlaceholderText(/Tushar Seth/i)).toBeInTheDocument(); // Name field should be present
        expect(screen.getByRole('button', { name: /Register/i })).toBeInTheDocument();
        expect(screen.queryByRole('button', { name: /Login/i })).not.toBeInTheDocument();
    });

    test('handles input changes in login form', () => {
        renderWithProviders(<Login />, {
            providerProps: { login: mockLogin, register: mockRegister }
        });

        const emailInput = screen.getByPlaceholderText(/tusharseth@example.com/i);
        const passwordInput = screen.getByPlaceholderText(/••••••••/i);

        fireEvent.change(emailInput, { target: { name: 'email', value: 'test@example.com' } });
        fireEvent.change(passwordInput, { target: { name: 'password', value: 'password123' } });

        expect(emailInput.value).toBe('test@example.com');
        expect(passwordInput.value).toBe('password123');
    });

    test('handles input changes in register form', () => {
        renderWithProviders(<Login />, {
            providerProps: { login: mockLogin, register: mockRegister }
        });

        fireEvent.click(screen.getByRole('button', { name: /Register/i }));

        const nameInput = screen.getByPlaceholderText(/Tushar Seth/i);
        const emailInput = screen.getByPlaceholderText(/tusharseth@example.com/i);
        const passwordInput = screen.getByPlaceholderText(/••••••••/i);

        fireEvent.change(nameInput, { target: { name: 'name', value: 'John Doe' } });
        fireEvent.change(emailInput, { target: { name: 'email', value: 'john.doe@example.com' } });
        fireEvent.change(passwordInput, { target: { name: 'password', value: 'securepass' } });

        expect(nameInput.value).toBe('John Doe');
        expect(emailInput.value).toBe('john.doe@example.com');
        expect(passwordInput.value).toBe('securepass');
    });

    test('successfully logs in a user', async () => {
        mockLogin.mockResolvedValue({ success: true });
        renderWithProviders(<Login />, {
            providerProps: { login: mockLogin, register: mockRegister }
        });

        fireEvent.change(screen.getByPlaceholderText(/tusharseth@example.com/i), { target: { name: 'email', value: 'test@example.com' } });
        fireEvent.change(screen.getByPlaceholderText(/••••••••/i), { target: { name: 'password', value: 'password123' } });
        fireEvent.click(screen.getByRole('button', { name: /Login/i }));

        await waitFor(() => {
            expect(mockLogin).toHaveBeenCalledWith('test@example.com', 'password123');
            expect(mockNavigate).toHaveBeenCalledWith('/dashboard');
        });
    });

    test('displays error on login failure', async () => {
        mockLogin.mockResolvedValue({ success: false, message: 'Invalid credentials' });
        renderWithProviders(<Login />, {
            providerProps: { login: mockLogin, register: mockRegister }
        });

        fireEvent.change(screen.getByPlaceholderText(/tusharseth@example.com/i), { target: { name: 'email', value: 'test@example.com' } });
        fireEvent.change(screen.getByPlaceholderText(/••••••••/i), { target: { name: 'password', value: 'wrongpass' } });
        fireEvent.click(screen.getByRole('button', { name: /Login/i }));

        await waitFor(() => {
            expect(mockLogin).toHaveBeenCalledWith('test@example.com', 'wrongpass');
            expect(screen.getByText('Invalid credentials')).toBeInTheDocument();
            expect(mockNavigate).not.toHaveBeenCalled();
        });
    });

    test('successfully registers a user', async () => {
        mockRegister.mockResolvedValue({ success: true });
        renderWithProviders(<Login />, {
            providerProps: { login: mockLogin, register: mockRegister }
        });

        fireEvent.click(screen.getByRole('button', { name: /Register/i }));

        fireEvent.change(screen.getByPlaceholderText(/Tushar Seth/i), { target: { name: 'name', value: 'Jane Doe' } });
        fireEvent.change(screen.getByPlaceholderText(/tusharseth@example.com/i), { target: { name: 'email', value: 'jane.doe@example.com' } });
        fireEvent.change(screen.getByPlaceholderText(/••••••••/i), { target: { name: 'password', value: 'newpass123' } });
        fireEvent.click(screen.getByRole('button', { name: /Register/i }));

        await waitFor(() => {
            expect(mockRegister).toHaveBeenCalledWith('Jane Doe', 'jane.doe@example.com', 'newpass123');
            expect(mockNavigate).toHaveBeenCalledWith('/dashboard');
        });
    });

    test('displays error on registration failure', async () => {
        mockRegister.mockResolvedValue({ success: false, message: 'Registration failed' });
        renderWithProviders(<Login />, {
            providerProps: { login: mockLogin, register: mockRegister }
        });

        fireEvent.click(screen.getByRole('button', { name: /Register/i }));

        fireEvent.change(screen.getByPlaceholderText(/Tushar Seth/i), { target: { name: 'name', value: 'Jane Doe' } });
        fireEvent.change(screen.getByPlaceholderText(/tusharseth@example.com/i), { target: { name: 'email', value: 'jane.doe@example.com' } });
        fireEvent.change(screen.getByPlaceholderText(/••••••••/i), { target: { name: 'password', value: 'newpass123' } });
        fireEvent.click(screen.getByRole('button', { name: /Register/i }));

        await waitFor(() => {
            expect(mockRegister).toHaveBeenCalledWith('Jane Doe', 'jane.doe@example.com', 'newpass123');
            expect(screen.getByText('Registration failed')).toBeInTheDocument();
            expect(mockNavigate).not.toHaveBeenCalled();
        });
    });

    test('OAuth2 Google login redirects to correct URL', () => {
        delete window.location;
        window.location = { href: '' };

        renderWithProviders(<Login />, {
            providerProps: { login: mockLogin, register: mockRegister }
        });

        fireEvent.click(screen.getByRole('button', { name: /Continue with Google/i }));

        const expectedUrl = `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'}/oauth2/authorization/google`;
        expect(window.location.href).toBe(expectedUrl);
    });

    test('OAuth2 GitHub login redirects to correct URL', () => {
        delete window.location;
        window.location = { href: '' };

        renderWithProviders(<Login />, {
            providerProps: { login: mockLogin, register: mockRegister }
        });

        fireEvent.click(screen.getByRole('button', { name: /Continue with GitHub/i }));

        const expectedUrl = `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'}/oauth2/authorization/github`;
        expect(window.location.href).toBe(expectedUrl);
    });

    test('handles OAuth2 redirect with tokens and navigates to dashboard', () => {
        useLocation.mockReturnValue({
            search: '?token=mockToken&refreshToken=mockRefreshToken',
            pathname: '/login',
            state: null,
            hash: '',
            key: 'default',
        });

        renderWithProviders(<Login />, {
            providerProps: { login: mockLogin, register: mockRegister }
        });

        expect(window.localStorage.setItem).toHaveBeenCalledWith('token', 'mockToken');
        expect(window.localStorage.setItem).toHaveBeenCalledWith('refreshToken', 'mockRefreshToken');
        expect(mockNavigate).toHaveBeenCalledWith('/dashboard');
    });

});

