(function() {
    'use strict';

    angular
        .module('backendApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('event-user-attending', {
            parent: 'entity',
            url: '/event-user-attending?page&sort&search',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'backendApp.eventUserAttending.home.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/event-user-attending/event-user-attendings.html',
                    controller: 'EventUserAttendingController',
                    controllerAs: 'vm'
                }
            },
            params: {
                page: {
                    value: '1',
                    squash: true
                },
                sort: {
                    value: 'id,asc',
                    squash: true
                },
                search: null
            },
            resolve: {
                pagingParams: ['$stateParams', 'PaginationUtil', function ($stateParams, PaginationUtil) {
                    return {
                        page: PaginationUtil.parsePage($stateParams.page),
                        sort: $stateParams.sort,
                        predicate: PaginationUtil.parsePredicate($stateParams.sort),
                        ascending: PaginationUtil.parseAscending($stateParams.sort),
                        search: $stateParams.search
                    };
                }],
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('eventUserAttending');
                    $translatePartialLoader.addPart('eventAttendingType');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('event-user-attending-detail', {
            parent: 'entity',
            url: '/event-user-attending/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'backendApp.eventUserAttending.detail.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/event-user-attending/event-user-attending-detail.html',
                    controller: 'EventUserAttendingDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('eventUserAttending');
                    $translatePartialLoader.addPart('eventAttendingType');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'EventUserAttending', function($stateParams, EventUserAttending) {
                    return EventUserAttending.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'event-user-attending',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('event-user-attending-detail.edit', {
            parent: 'event-user-attending-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/event-user-attending/event-user-attending-dialog.html',
                    controller: 'EventUserAttendingDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['EventUserAttending', function(EventUserAttending) {
                            return EventUserAttending.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('event-user-attending.new', {
            parent: 'event-user-attending',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/event-user-attending/event-user-attending-dialog.html',
                    controller: 'EventUserAttendingDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                createdDate: null,
                                type: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('event-user-attending', null, { reload: 'event-user-attending' });
                }, function() {
                    $state.go('event-user-attending');
                });
            }]
        })
        .state('event-user-attending.edit', {
            parent: 'event-user-attending',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/event-user-attending/event-user-attending-dialog.html',
                    controller: 'EventUserAttendingDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['EventUserAttending', function(EventUserAttending) {
                            return EventUserAttending.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('event-user-attending', null, { reload: 'event-user-attending' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('event-user-attending.delete', {
            parent: 'event-user-attending',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/event-user-attending/event-user-attending-delete-dialog.html',
                    controller: 'EventUserAttendingDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['EventUserAttending', function(EventUserAttending) {
                            return EventUserAttending.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('event-user-attending', null, { reload: 'event-user-attending' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
