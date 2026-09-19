import { test, expect } from '@playwright/test';

test.describe('Testes da Tela de Login', () => {

  test('Login com credenciais válidas', async ({ page }) => {
    await page.goto('http://127.0.0.1:3000/login');
    
    await page.fill('#email', 'ana@exemplo.com');
    await page.fill('#senha', 'SenhaSegura123!');
    await page.click('button[type="submit"]');

    await expect(page).toHaveURL('http://127.0.0.1:3000/dashboard');
    await expect(page.locator('h1')).toContainText('Bem-vindo');
  });

  test('Login com credenciais inválidas', async ({ page }) => {
    await page.goto('http://127.0.0.1:3000/login');
    
    await page.fill('#email', 'usuario@invalido.com');
    await page.fill('#senha', 'errada123');
    await page.click('button[type="submit"]');

    await expect(page.locator('.error-message')).toBeVisible();
  });

});
