(function() {
    'use strict';

    angular
        .module('backendApp')
        .controller('EventCategoryDetailController', EventCategoryDetailController);

    EventCategoryDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'DataUtils', 'entity', 'EventCategory', 'Event'];

    function EventCategoryDetailController($scope, $rootScope, $stateParams, previousState, DataUtils, entity, EventCategory, Event) {
        var vm = this;

        vm.eventCategory = entity;
        vm.previousState = previousState.name;
        vm.byteSize = DataUtils.byteSize;
        vm.openFile = DataUtils.openFile;

        var unsubscribe = $rootScope.$on('backendApp:eventCategoryUpdate', function(event, result) {
            vm.eventCategory = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
