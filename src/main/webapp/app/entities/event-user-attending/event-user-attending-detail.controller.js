(function() {
    'use strict';

    angular
        .module('backendApp')
        .controller('EventUserAttendingDetailController', EventUserAttendingDetailController);

    EventUserAttendingDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'EventUserAttending', 'Event', 'User'];

    function EventUserAttendingDetailController($scope, $rootScope, $stateParams, previousState, entity, EventUserAttending, Event, User) {
        var vm = this;

        vm.eventUserAttending = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('backendApp:eventUserAttendingUpdate', function(event, result) {
            vm.eventUserAttending = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
