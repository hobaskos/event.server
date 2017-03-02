'use strict';

describe('Controller Tests', function() {

    describe('EventPoll Management Detail Controller', function() {
        var $scope, $rootScope;
        var MockEntity, MockPreviousState, MockEventPoll, MockEvent, MockEventImage;
        var createController;

        beforeEach(inject(function($injector) {
            $rootScope = $injector.get('$rootScope');
            $scope = $rootScope.$new();
            MockEntity = jasmine.createSpy('MockEntity');
            MockPreviousState = jasmine.createSpy('MockPreviousState');
            MockEventPoll = jasmine.createSpy('MockEventPoll');
            MockEvent = jasmine.createSpy('MockEvent');
            MockEventImage = jasmine.createSpy('MockEventImage');
            

            var locals = {
                '$scope': $scope,
                '$rootScope': $rootScope,
                'entity': MockEntity,
                'previousState': MockPreviousState,
                'EventPoll': MockEventPoll,
                'Event': MockEvent,
                'EventImage': MockEventImage
            };
            createController = function() {
                $injector.get('$controller')("EventPollDetailController", locals);
            };
        }));


        describe('Root Scope Listening', function() {
            it('Unregisters root scope listener upon scope destruction', function() {
                var eventType = 'backendApp:eventPollUpdate';

                createController();
                expect($rootScope.$$listenerCount[eventType]).toEqual(1);

                $scope.$destroy();
                expect($rootScope.$$listenerCount[eventType]).toBeUndefined();
            });
        });
    });

});
