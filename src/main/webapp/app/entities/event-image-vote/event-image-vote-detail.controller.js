(function() {
    'use strict';

    angular
        .module('backendApp')
        .controller('EventImageVoteDetailController', EventImageVoteDetailController);

    EventImageVoteDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'EventImageVote', 'User', 'EventImage'];

    function EventImageVoteDetailController($scope, $rootScope, $stateParams, previousState, entity, EventImageVote, User, EventImage) {
        var vm = this;

        vm.eventImageVote = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('backendApp:eventImageVoteUpdate', function(event, result) {
            vm.eventImageVote = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
