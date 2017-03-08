'use strict';

describe('Controller Tests', function() {

    describe('EventImageVote Management Detail Controller', function() {
        var $scope, $rootScope;
        var MockEntity, MockPreviousState, MockEventImageVote, MockUser, MockEventImage;
        var createController;

        beforeEach(inject(function($injector) {
            $rootScope = $injector.get('$rootScope');
            $scope = $rootScope.$new();
            MockEntity = jasmine.createSpy('MockEntity');
            MockPreviousState = jasmine.createSpy('MockPreviousState');
            MockEventImageVote = jasmine.createSpy('MockEventImageVote');
            MockUser = jasmine.createSpy('MockUser');
            MockEventImage = jasmine.createSpy('MockEventImage');
            

            var locals = {
                '$scope': $scope,
                '$rootScope': $rootScope,
                'entity': MockEntity,
                'previousState': MockPreviousState,
                'EventImageVote': MockEventImageVote,
                'User': MockUser,
                'EventImage': MockEventImage
            };
            createController = function() {
                $injector.get('$controller')("EventImageVoteDetailController", locals);
            };
        }));


        describe('Root Scope Listening', function() {
            it('Unregisters root scope listener upon scope destruction', function() {
                var eventType = 'backendApp:eventImageVoteUpdate';

                createController();
                expect($rootScope.$$listenerCount[eventType]).toEqual(1);

                $scope.$destroy();
                expect($rootScope.$$listenerCount[eventType]).toBeUndefined();
            });
        });
    });

});
