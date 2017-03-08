(function() {
    'use strict';

    angular
        .module('backendApp')
        .controller('EventPollDetailController', EventPollDetailController);

    EventPollDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'EventPoll', 'Event'];

    function EventPollDetailController($scope, $rootScope, $stateParams, previousState, entity, EventPoll, Event) {
        var vm = this;

        vm.eventPoll = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('backendApp:eventPollUpdate', function(event, result) {
            vm.eventPoll = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
