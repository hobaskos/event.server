'use strict';

describe('EventUserAttending e2e test', function () {

    var username = element(by.id('username'));
    var password = element(by.id('password'));
    var entityMenu = element(by.id('entity-menu'));
    var accountMenu = element(by.id('account-menu'));
    var login = element(by.id('login'));
    var logout = element(by.id('logout'));

    beforeAll(function () {
        browser.get('/');

        accountMenu.click();
        login.click();

        username.sendKeys('admin');
        password.sendKeys('admin');
        element(by.css('button[type=submit]')).click();
    });

    it('should load EventUserAttendings', function () {
        entityMenu.click();
        element.all(by.css('[ui-sref="event-user-attending"]')).first().click().then(function() {
            element.all(by.css('h2')).first().getAttribute('data-translate').then(function (value) {
                expect(value).toMatch(/backendApp.eventUserAttending.home.title/);
            });
        });
    });

    it('should load create EventUserAttending dialog', function () {
        element(by.css('[ui-sref="event-user-attending.new"]')).click().then(function() {
            element(by.css('h4.modal-title')).getAttribute('data-translate').then(function (value) {
                expect(value).toMatch(/backendApp.eventUserAttending.home.createOrEditLabel/);
            });
            element(by.css('button.close')).click();
        });
    });

    afterAll(function () {
        accountMenu.click();
        logout.click();
    });
});
